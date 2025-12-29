package com.hackathon.databeats.churninsight.infra.adapter.output.persistence.mapper;

import com.hackathon.databeats.churninsight.domain.model.CustomerProfile;
import com.hackathon.databeats.churninsight.domain.model.PredictionResult;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.entity.PredictionHistory;
import org.springframework.stereotype.Component;

@Component
public class PersistenceMapper {

    public PredictionHistory toEntity(CustomerProfile profile, PredictionResult result) {
        PredictionHistory entity = new PredictionHistory();
        
        // ID e resultado da predição
        entity.setId(result.id());
        entity.setChurnLabel(result.status());
        entity.setWillChurn("Vai cancelar".equals(result.status()));
        entity.setProbability(result.probability());
        
        // Dados de entrada do perfil do cliente
        entity.setGender(profile.gender());
        entity.setAge(profile.age());
        entity.setCountry(profile.country());
        entity.setSubscriptionType(profile.subscriptionType());
        entity.setListeningTime(profile.listeningTime());
        entity.setSongsPlayed(profile.songsPlayedPerDay());
        entity.setSkipRate(profile.skipRate());
        entity.setDeviceType(profile.deviceType());
        entity.setOfflineListening(profile.offlineListening());
        
        return entity;
    }

    public PredictionResult toDomain(PredictionHistory entity) {
        return new PredictionResult(
                entity.getId(),
                entity.getChurnLabel() != null ? entity.getChurnLabel() : 
                    (Boolean.TRUE.equals(entity.getWillChurn()) ? "Vai cancelar" : "Vai continuar"),
                entity.getProbability()
        );
    }
}
