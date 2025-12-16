package developer.ezandro.churninsight.infra.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StatsResponse(
        @JsonProperty("total_avaliados")
        long totalEvaluated,

        @JsonProperty("taxa_churn")
        double churnRate
) {}