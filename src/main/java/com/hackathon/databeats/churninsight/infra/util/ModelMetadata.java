package com.hackathon.databeats.churninsight.infra.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ModelMetadata {
    private String name;
    private String version;
    private double accuracy;

    @JsonProperty(value = "f1_score")
    private double f1Score;

    @JsonProperty(value = "numeric_features")
    private List<String> numericFeatures;

    @JsonProperty(value = "categorical_features")
    private List<String> categoricalFeatures;

    @JsonProperty(value = "model_type")
    private String modelType;

    private double precision;
    private double recall;

    @JsonProperty(value = "auc_roc")
    private double aucRoc;

    @JsonProperty(value = "threshold_otimo")
    private double thresholdOtimo;

    @JsonProperty(value = "export_date")
    private String exportDate;
}