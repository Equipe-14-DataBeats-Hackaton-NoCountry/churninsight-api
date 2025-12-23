package com.hackathon.databeats.churninsight.infra.adapter.output.persistence;

import com.hackathon.databeats.churninsight.application.port.output.SaveHistoryPort;
import com.hackathon.databeats.churninsight.domain.model.PredictionResult;
import com.hackathon.databeats.churninsight.infra.adapter.input.web.dto.StatsResponse;

public class InMemoryHistoryAdapter implements SaveHistoryPort {

    private long totalEvaluated = 0;
    private long totalChurn = 0;

    @Override
    public void savePrediction(PredictionResult result) {
        this.totalEvaluated++;
        if ("Vai cancelar".equals(result.status())) {
            this.totalChurn++;
        }
    }

    public StatsResponse getStats() {
        double rate = totalEvaluated == 0 ? 0.0 : (double) totalChurn / totalEvaluated;
        return new StatsResponse(totalEvaluated, rate);
    }
}