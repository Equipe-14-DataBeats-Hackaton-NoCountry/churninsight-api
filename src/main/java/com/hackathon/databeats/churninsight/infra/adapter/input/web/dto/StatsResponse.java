package com.hackathon.databeats.churninsight.infra.adapter.input.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StatsResponse(
        @JsonProperty("total_avaliados")
        long totalEvaluated,

        @JsonProperty("taxa_churn")
        double churnRate
) { }