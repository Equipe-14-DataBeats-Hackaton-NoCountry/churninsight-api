package com.hackathon.databeats.churninsight.application.port.output;

import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.repository.PredictionHistoryEntity;

public interface SaveHistoryPort {
    void save(PredictionHistoryEntity history);
}