package com.hackathon.databeats.churninsight.infra.adapter.output.inference;

import com.hackathon.databeats.churninsight.application.port.output.LoadModelPort;
import com.hackathon.databeats.churninsight.domain.model.CustomerProfile;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class MockModelAdapter implements LoadModelPort {

    @Override
    public double calculateProbability(CustomerProfile r, boolean isHighRisk) {
        double probability;

        if (isHighRisk) {
            probability = 0.70
                    + Math.min(0.20, r.skipRate() * 0.05)
                    + randomBetween(0.05);
            probability = Math.min(probability, 0.95);
        } else {
            probability = 0.10
                    + randomBetween(0.30)
                    - Math.min(0.25, r.listeningTime() / 100.0);
            probability = Math.max(probability, 0.01);
        }
        return probability;
    }

    private double randomBetween(double max) {
        return ThreadLocalRandom.current().nextDouble(max);
    }
}