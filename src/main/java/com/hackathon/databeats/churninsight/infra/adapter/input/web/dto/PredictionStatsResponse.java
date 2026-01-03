package com.hackathon.databeats.churninsight.infra.adapter.input.web.dto;

import com.hackathon.databeats.churninsight.domain.enums.ChurnStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionStatsResponse {
    private ChurnStatus label;
    private double probability;
    private float[] probabilities;
    private Map<String, Float> classProbabilities;
}