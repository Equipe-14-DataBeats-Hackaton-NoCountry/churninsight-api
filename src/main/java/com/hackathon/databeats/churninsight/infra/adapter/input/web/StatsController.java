package com.hackathon.databeats.churninsight.infra.adapter.input.web;

import com.hackathon.databeats.churninsight.application.port.input.GetStatsUseCase;
import com.hackathon.databeats.churninsight.infra.adapter.input.web.dto.StatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private final GetStatsUseCase useCase;

    @GetMapping
    public ResponseEntity<StatsResponse> getStats() {
        return ResponseEntity.ok(useCase.execute());
    }
}