package com.hackathon.databeats.churninsight.infra.adapter.input.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StatsResponse(
        @JsonProperty(value = "total_avaliados")
        long totalEvaluated,

        @JsonProperty(value = "taxa_churn")
        double churnRate
) {}