package com.hackathon.databeats.churninsight.application.port.input;

import com.hackathon.databeats.churninsight.domain.model.CustomerProfile;
import com.hackathon.databeats.churninsight.infra.adapter.input.web.dto.PredictionStatsResponse;

public interface PredictionStatsUseCase {
    PredictionStatsResponse predictWithStats(CustomerProfile profile, String requesterId, String requestIp);
}