package com.hackathon.databeats.churninsight.application.service;

import com.hackathon.databeats.churninsight.infra.adapter.input.web.dto.DashboardMetricsResponse;
import com.hackathon.databeats.churninsight.domain.enums.ChurnStatus;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.repository.PredictionHistoryRepository;
import com.hackathon.databeats.churninsight.infra.util.ModelMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardMetricsService {
    private final PredictionHistoryRepository predictionHistoryRepository;
    private final ModelMetadata modelMetadata;

    public DashboardMetricsResponse getMetrics() {
        // 1 - Total de clientes (fonte única: histórico persistido)
        long totalCustomers = this.predictionHistoryRepository.count();

        // 2 - Clientes em risco
        long customersAtRisk =
                this.predictionHistoryRepository.countByChurnStatus(ChurnStatus.WILL_CHURN);

        // 3️- Taxa global de churn (%)
        double globalChurnRate = totalCustomers > 0
                ? (customersAtRisk * 100.0) / totalCustomers
                : 0.0;

        // 4️- Receita em risco (heurística por plano)
        double revenueAtRisk = this.calculateRevenueAtRisk();

        // 5️- Precisão do modelo (SEM arredondamento para mostrar 50.9%)
        double modelAccuracy = this.modelMetadata.getAccuracy();

        return DashboardMetricsResponse.builder()
                .totalCustomers(totalCustomers)
                .customersAtRisk(customersAtRisk)
                .globalChurnRate(round(globalChurnRate))
                .revenueAtRisk(round(revenueAtRisk))
                .modelAccuracy(modelAccuracy)
                .build();
    }

    /**
     * Regra simples de receita estimada por tipo de assinatura.
     * MVP: valores fixos por plano.
     */
    private double calculateRevenueAtRisk() {
        // Definição dos valores reais por plano
        Map<String, Double> planValues = Map.of(
                "Premium", 23.90,
                "Family", 40.90,
                "Duo", 31.90,
                "Student", 12.90,
                "Free", 0.0
        );

        // Para cada plano, conta clientes em risco e multiplica pelo valor
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

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}