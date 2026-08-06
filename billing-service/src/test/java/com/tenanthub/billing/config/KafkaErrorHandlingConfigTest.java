package com.tenanthub.billing.config;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaErrorHandlingConfigTest {

    @Test
    void kafkaErrorHandler_isConfigured() {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, Object> kafkaTemplate = Mockito.mock(KafkaTemplate.class);

        DefaultErrorHandler handler = new KafkaErrorHandlingConfig().kafkaErrorHandler(kafkaTemplate);

        assertThat(handler).isNotNull();
    }
}
