package com.hackathon.databeats.churninsight.application.port.output;

import com.hackathon.databeats.churninsight.domain.model.CustomerProfile;

public interface LoadModelPort {
    double calculateProbability(CustomerProfile profile, boolean isHighRisk);
    boolean isModelLoaded();
}