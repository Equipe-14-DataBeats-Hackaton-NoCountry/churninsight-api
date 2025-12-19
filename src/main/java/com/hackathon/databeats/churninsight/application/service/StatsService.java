package com.hackathon.databeats.churninsight.application.service;

import com.hackathon.databeats.churninsight.application.port.input.GetStatsUseCase;
import com.hackathon.databeats.churninsight.application.port.output.SaveHistoryPort;
import com.hackathon.databeats.churninsight.infra.adapter.input.web.dto.StatsResponse;

public class StatsService implements GetStatsUseCase {

    private final SaveHistoryPort historyPort;

    public StatsService(SaveHistoryPort historyPort) {
        this.historyPort = historyPort;
    }

    @Override
    public StatsResponse execute() {
        return historyPort.getStats();
    }
}