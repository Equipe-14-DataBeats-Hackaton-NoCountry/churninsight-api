package com.hackathon.databeats.churninsight.infra.adapter.output.persistence.mapper;

import com.hackathon.databeats.churninsight.domain.model.PredictionResult;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.entity.PredictionHistory;
import org.springframework.stereotype.Component;

@Component
public class PersistenceMapper {

    public PredictionHistory toEntity(PredictionResult result) {
        PredictionHistory entity = new PredictionHistory();
        entity.setId(result.id());
        entity.setChurnStatus(result.status());
        entity.setProbability(result.probability());
        return entity;
    }

    public PredictionResult toDomain(PredictionHistory entity) {
        return new PredictionResult(
                entity.getId(),
                entity.getChurnStatus(),
                entity.getProbability()
        );
    }
}
