package com.hackathon.databeats.churninsight.infra.adapter.output.persistence;

import com.hackathon.databeats.churninsight.application.port.output.SaveHistoryPort;
import com.hackathon.databeats.churninsight.domain.model.CustomerProfile;
import com.hackathon.databeats.churninsight.domain.model.PredictionResult;
import com.hackathon.databeats.churninsight.infra.adapter.input.web.dto.StatsResponse;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.entity.PredictionHistory;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.mapper.PersistenceMapper;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.repository.PredictionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MySQLHistoryAdapter implements SaveHistoryPort {

    private final PredictionRepository repository;
    private final PersistenceMapper mapper;

    @Override
    @Transactional
    public void savePrediction(CustomerProfile profile, PredictionResult result) {
        PredictionHistory entity = mapper.toEntity(profile, result);
        repository.saveAndFlush(entity);
    }

    @Override
    public StatsResponse getStats() {
        long totalEvaluated = repository.count();
        long totalChurn = repository.countChurnPredictions();
        
        double rate = totalEvaluated == 0 ? 0.0 : (double) totalChurn / totalEvaluated;
        
        return new StatsResponse(totalEvaluated, rate);
    }
}
