package com.hackathon.databeats.churninsight.infra.adapter.input.web.dto;

import java.util.UUID;

public record ChurnResponse(
        UUID id,
        String churnStatus,
        Double churnProbability
) {
}
