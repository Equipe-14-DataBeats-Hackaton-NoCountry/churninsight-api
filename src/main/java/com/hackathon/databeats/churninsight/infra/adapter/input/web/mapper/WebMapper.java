package com.hackathon.databeats.churninsight.infra.adapter.input.web.mapper;

import com.hackathon.databeats.churninsight.domain.enums.ChurnStatus;
import com.hackathon.databeats.churninsight.domain.model.CustomerProfile;
import com.hackathon.databeats.churninsight.domain.model.PredictionResult;
import com.hackathon.databeats.churninsight.infra.adapter.input.web.dto.ChurnRequest;
import com.hackathon.databeats.churninsight.infra.adapter.input.web.dto.ChurnResponse;
import org.springframework.stereotype.Component;

@Component
public class WebMapper {

    public CustomerProfile toDomain(ChurnRequest request) {
        return new CustomerProfile(
                request.gender(), request.age(), request.country(),
                request.subscriptionType(), request.listeningTime(),
                request.songsPlayedPerDay(), request.skipRate(),
                request.deviceType(), request.offlineListening()
        );
    }

    public ChurnResponse toResponse(PredictionResult result) {
        return new ChurnResponse(
                result.id(),
                ChurnStatus.valueOf(result.status()),
                result.probability()
        );
    }
}