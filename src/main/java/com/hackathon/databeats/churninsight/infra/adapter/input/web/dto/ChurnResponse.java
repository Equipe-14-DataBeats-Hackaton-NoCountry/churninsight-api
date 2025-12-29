package com.hackathon.databeats.churninsight.infra.adapter.input.web.dto;

import com.hackathon.databeats.churninsight.domain.enums.ChurnStatus;

import java.util.UUID;

public record ChurnResponse(
        UUID id,
        ChurnStatus churnStatus,
        Double churnProbability
) {}