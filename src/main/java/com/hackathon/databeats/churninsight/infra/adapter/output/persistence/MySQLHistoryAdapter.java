package com.hackathon.databeats.churninsight.infra.adapter.output.persistence;

import com.hackathon.databeats.churninsight.application.port.output.SaveHistoryPort;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.repository.PredictionHistoryEntity;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.repository.PredictionHistoryRepository;
import org.springframework.stereotype.Component;

@Component
public class MySQLHistoryAdapter implements SaveHistoryPort {
    private final PredictionHistoryRepository repository;

    public MySQLHistoryAdapter(PredictionHistoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(PredictionHistoryEntity history) {
        this.repository.save(history);
    }
}