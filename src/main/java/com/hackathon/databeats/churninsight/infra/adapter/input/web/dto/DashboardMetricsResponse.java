package com.hackathon.databeats.churninsight.infra.adapter.input.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardMetricsResponse {
    @JsonProperty("total_customers")
    private long totalCustomers;

    @JsonProperty("global_churn_rate")
    private double globalChurnRate; // %

    @JsonProperty("customers_at_risk")
    private long customersAtRisk;

    @JsonProperty("revenue_at_risk")
    private double revenueAtRisk;

    @JsonProperty("model_accuracy")
    private double modelAccuracy;   // 0..1 (ex: 0.6488)

    /**
     * Distribuição para o gráfico: [willStay, willChurn]
     */
    @JsonProperty("churn_distribution")
    private List<Long> churnDistribution;

    /**
     * Lista consolidada de fatores de risco (top N)
     */
    @JsonProperty("risk_factors")
    private List<RiskFactorItem> riskFactors;

    /**
     * Lista para gráfico (proxy baseado em frequência)
     */
    @JsonProperty("feature_importance")
    private List<FeatureImportanceItem> featureImportance;

    @Data
    @Builder
    public static class RiskFactorItem {
        @JsonProperty("name")
        private String name;

        @JsonProperty("count")
        private long count;

        @JsonProperty("percentage")
        private double percentage; // 0..100
    }

    @Data
    @Builder
    public static class FeatureImportanceItem {
        @JsonProperty("name")
        private String name;

        @JsonProperty("value")
        private double value; // 0..1 (normalizado)
    }
}