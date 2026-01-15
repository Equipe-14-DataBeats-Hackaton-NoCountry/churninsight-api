package com.hackathon.databeats.churninsight.application.service;

import com.hackathon.databeats.churninsight.domain.enums.ChurnStatus;
import com.hackathon.databeats.churninsight.infra.adapter.input.web.dto.DashboardMetricsResponse;
import com.hackathon.databeats.churninsight.infra.adapter.input.web.dto.DashboardMetricsResponse.FeatureImportanceItem;
import com.hackathon.databeats.churninsight.infra.adapter.input.web.dto.DashboardMetricsResponse.RiskFactorItem;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.repository.PredictionHistoryRepository;
import com.hackathon.databeats.churninsight.infra.util.ModelMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardMetricsService {

    private final PredictionHistoryRepository predictionHistoryRepository;
    private final ModelMetadata modelMetadata;

    public DashboardMetricsResponse getMetrics() {
        // 1 - Total de clientes (fonte única: histórico persistido)
        long totalCustomers = this.predictionHistoryRepository.count();

        // 2 - Clientes em risco (TOP 25% por probabilidade)  -> regra Mariana
        Long customersAtRisk = this.predictionHistoryRepository.countTop25AtRisk();
        if (customersAtRisk == null) customersAtRisk = 0L;

        // 3 - Clientes em monitoramento (%) = TOP 25% (não é WILL_CHURN)
        double monitoringRate = totalCustomers > 0
                ? (customersAtRisk * 100.0) / totalCustomers
                : 0.0;

        // 4 - Receita em risco (mantém como está HOJE: baseada em WILL_CHURN)
        double revenueAtRisk = this.calculateRevenueAtRisk();

        // 5 - Precisão do modelo (0..1)
        double modelAccuracy = this.modelMetadata.getAccuracy();

        // 6 - Distribuição (para gráfico): [willStay, willChurn] (mantém)
        List<Long> churnDistribution = buildChurnDistribution();

        // 7 - Principais fatores (consolidado no backend)
        List<RiskFactorItem> riskFactors = buildRiskFactors(totalCustomers);

        // 8 - Feature importance (proxy baseado em frequência dos fatores)
        List<FeatureImportanceItem> featureImportance = buildFeatureImportanceFromRiskFactors(riskFactors);

        return DashboardMetricsResponse.builder()
                .totalCustomers(totalCustomers)
                .customersAtRisk(customersAtRisk)
                .globalChurnRate(round(monitoringRate))
                .revenueAtRisk(round2(revenueAtRisk))
                .modelAccuracy(modelAccuracy)
                .churnDistribution(churnDistribution)
                .riskFactors(riskFactors)
                .featureImportance(featureImportance)
                .build();
    }

    /**
     * [willStay, willChurn]
     */
    private List<Long> buildChurnDistribution() {
        long willChurn = predictionHistoryRepository.countByChurnStatus(ChurnStatus.WILL_CHURN);
        long willStay = predictionHistoryRepository.countByChurnStatus(ChurnStatus.WILL_STAY);
        return List.of(willStay, willChurn);
    }

    /**
     * Fatores de risco: usa query agregada já existente no repo.
     * Repo retorna (na ordem): free+ads, skipHigh, frustHigh, premiumNoOffline, lowListeningTime
     */
    private List<RiskFactorItem> buildRiskFactors(long totalCustomers) {
        Object[] raw = predictionHistoryRepository.getRiskFactorCounts();
        if (raw == null || raw.length < 5) return List.of();

        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("Anúncios por Semana", safeLong(raw[0]));
        counts.put("Taxa de Pulos Elevada", safeLong(raw[1]));
        counts.put("Índice de Frustração Alto", safeLong(raw[2]));
        counts.put("Subutilização Premium", safeLong(raw[3]));
        counts.put("Tempo de Escuta Baixo", safeLong(raw[4]));

        long denom = Math.max(1, totalCustomers);

        return counts.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(10)
                .map(e -> RiskFactorItem.builder()
                        .name(e.getKey())
                        .count(e.getValue())
                        .percentage(round((e.getValue() * 100.0) / denom))
                        .build())
                .toList();
    }

    /**
     * Proxy para o gráfico: normaliza pela soma dos fatores (0..1).
     */
    private List<FeatureImportanceItem> buildFeatureImportanceFromRiskFactors(List<RiskFactorItem> riskFactors) {
        if (riskFactors == null || riskFactors.isEmpty()) return List.of();

        long sum = riskFactors.stream().mapToLong(RiskFactorItem::getCount).sum();
        if (sum <= 0) return List.of();

        return riskFactors.stream()
                .map(rf -> FeatureImportanceItem.builder()
                        .name(rf.getName())
                        .value((double) rf.getCount() / (double) sum)
                        .build())
                .toList();
    }

    /**
     * Regra simples de receita estimada por tipo de assinatura.
     */
    private double calculateRevenueAtRisk() {
        Map<String, Double> planValues = Map.of(
                "Premium", 23.90,
                "Family", 40.90,
                "Duo", 31.90,
                "Student", 12.90,
                "Free", 0.0
        );

        return planValues.entrySet().stream()
                .mapToDouble(entry -> {
                    long count = this.predictionHistoryRepository.count((root, query, cb) ->
                            cb.and(
                                    cb.equal(root.get("churnStatus"), ChurnStatus.WILL_CHURN),
                                    cb.equal(root.get("subscriptionType"), entry.getKey())
                            ));
                    return count * entry.getValue();
                })
                .sum();
    }

    private long safeLong(Object v) {
        if (v == null) return 0L;
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(String.valueOf(v)); } catch (Exception e) { return 0L; }
    }

    // 1 casa decimal (para % e números gerais)
    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    // 2 casas (pra dinheiro)
    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}