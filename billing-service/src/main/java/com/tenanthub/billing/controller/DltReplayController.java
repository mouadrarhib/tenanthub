package com.tenanthub.billing.controller;

import com.tenanthub.billing.dto.DltReplayResponse;
import com.tenanthub.billing.service.DltReplayService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dlt")
@RequiredArgsConstructor
public class DltReplayController {

    private final DltReplayService dltReplayService;

    @PostMapping("/replay")
    public DltReplayResponse replay(@RequestParam String topic) {
        int replayedCount = dltReplayService.replay(topic);
        return new DltReplayResponse(replayedCount);
    }
}
