package com.hackathon.databeats.churninsight.application.port.output;

import com.hackathon.databeats.churninsight.domain.model.CustomerProfile;
import com.hackathon.databeats.churninsight.domain.model.PredictionResult;
import com.hackathon.databeats.churninsight.infra.adapter.input.web.dto.StatsResponse;

public interface SaveHistoryPort {
    void savePrediction(CustomerProfile profile, PredictionResult result);
    StatsResponse getStats();
}
