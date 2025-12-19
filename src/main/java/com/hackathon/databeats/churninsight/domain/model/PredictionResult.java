package com.hackathon.databeats.churninsight.domain.model;

import java.util.UUID;

public record PredictionResult(
        UUID id,
        String status,
        Double probability
) {}
