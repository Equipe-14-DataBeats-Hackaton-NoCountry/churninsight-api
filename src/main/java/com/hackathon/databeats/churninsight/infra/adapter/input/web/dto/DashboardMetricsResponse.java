package com.hackathon.databeats.churninsight.infra.adapter.input.web.dto;

import lombok.Builder;

@Builder
public record DashboardMetricsResponse(
        long totalCustomers,
        double globalChurnRate,
        long customersAtRisk,
        double revenueAtRisk,
        double modelAccuracy
) {}