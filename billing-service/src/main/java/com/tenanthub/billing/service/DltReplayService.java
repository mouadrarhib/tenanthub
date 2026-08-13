package com.tenanthub.billing.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@Service
@Slf4j
public class DltReplayService {

    private static final String CONSUMER_GROUP_ID = "dlt-replay-tool";
    private static final Duration POLL_TIMEOUT = Duration.ofSeconds(2);
    private static final int MAX_ASSIGNMENT_POLLS = 10;
    // Secondary safety net on top of the offset ceiling below - shouldn't normally be
    // needed, but stops the loop if polling ever comes back empty despite offsets
    // saying there's still work left.
    private static final int MAX_CONSECUTIVE_EMPTY_POLLS = 3;

    private final String bootstrapServers;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public DltReplayService(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
                             KafkaTemplate<String, Object> kafkaTemplate,
                             ObjectMapper objectMapper) {
        this.bootstrapServers = bootstrapServers;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    // Manual consumer, not @KafkaListener - this is a one-shot admin drain, not a
    // long-running listener. Its own consumer group (dlt-replay-tool) commits offsets
    // normally, so a message is only ever replayed once across calls: each call picks
    // up wherever the last one left off.
    public int replay(String topic) {
        String dltTopic = topic + "-dlt";

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        int replayedCount = 0;
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(List.of(dltTopic));

            // ceilings/resumeOffsets are null until the first poll() that yields a
            // partition assignment - and that same poll() may already carry records,
            // so it's fed into the loop below rather than discarded.
            Map<TopicPartition, Long> ceilings = null;
            Map<TopicPartition, OffsetAndMetadata> resumeOffsets = null;
            int consecutiveEmptyPolls = 0;
            int pollsSinceStart = 0;

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(POLL_TIMEOUT);
                pollsSinceStart++;

                if (ceilings == null) {
                    Set<TopicPartition> assignment = consumer.assignment();
                    if (assignment.isEmpty()) {
                        if (pollsSinceStart >= MAX_ASSIGNMENT_POLLS) {
                            log.warn("Could not get partition assignment for {} within {} polls - nothing replayed",
                                    dltTopic, MAX_ASSIGNMENT_POLLS);
                            return 0;
                        }
                        continue;
                    }

                    // Snapshot exactly what's sitting in the DLT right now, and where we
                    // last left off (per the group's own committed offsets, not the
                    // consumer's internal fetch position - that position may already be
                    // past records in `records` above that haven't been processed yet).
                    // A message that fails again once republished bounces straight back
                    // into this same DLT with an offset >= this ceiling, so it's left for
                    // a future call instead of being replayed again in this same loop.
                    ceilings = consumer.endOffsets(assignment);
                    resumeOffsets = new HashMap<>();
                    Map<TopicPartition, OffsetAndMetadata> committed = consumer.committed(assignment);
                    for (TopicPartition tp : assignment) {
                        OffsetAndMetadata committedOffset = committed.get(tp);
                        long startOffset = committedOffset != null ? committedOffset.offset() : 0L;
                        resumeOffsets.put(tp, new OffsetAndMetadata(startOffset));
                    }
                }

                if (records.isEmpty()) {
                    consecutiveEmptyPolls++;
                } else {
                    consecutiveEmptyPolls = 0;
                    for (ConsumerRecord<String, String> record : records) {
                        TopicPartition tp = new TopicPartition(record.topic(), record.partition());
                        long nextExpectedOffset = resumeOffsets.get(tp).offset();
                        if (record.offset() < nextExpectedOffset || record.offset() >= ceilings.get(tp)) {
                            continue;
                        }

                        JsonNode payload = objectMapper.readTree(record.value());
                        // Blocks until the broker acks the republish, so we never advance
                        // past a message that didn't actually make it back onto the topic.
                        kafkaTemplate.send(topic, record.key(), payload).get();
                        log.info("Replayed DLT message key={} offset={} from {} to {}",
                                record.key(), record.offset(), dltTopic, topic);
                        replayedCount++;
                        resumeOffsets.put(tp, new OffsetAndMetadata(record.offset() + 1));
                    }
                }

                boolean pendingWork = false;
                for (Map.Entry<TopicPartition, Long> entry : ceilings.entrySet()) {
                    if (entry.getValue() > resumeOffsets.get(entry.getKey()).offset()) {
                        pendingWork = true;
                        break;
                    }
                }
                if (!pendingWork || consecutiveEmptyPolls >= MAX_CONSECUTIVE_EMPTY_POLLS) {
                    break;
                }
            }

            if (resumeOffsets != null) {
                consumer.commitSync(resumeOffsets);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while replaying messages from " + dltTopic, e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to replay messages from " + dltTopic, e);
        }

        return replayedCount;
    }
}
