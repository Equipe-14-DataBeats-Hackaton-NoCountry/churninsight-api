package com.hackathon.databeats.churninsight.domain.rules;

import com.hackathon.databeats.churninsight.domain.model.CustomerProfile;
import com.hackathon.databeats.churninsight.infra.config.MetadataConfig;


public class RiskEvaluator {
    private final MetadataConfig config;

    public RiskEvaluator(MetadataConfig config) {
        this.config = config;
    }

    public boolean isHighRisk(CustomerProfile p) {
        return p.skipRate() >= 2.0 ||
                p.listeningTime() < 6.0 ||
                p.songsPlayedPerDay() < 10;
    }
}