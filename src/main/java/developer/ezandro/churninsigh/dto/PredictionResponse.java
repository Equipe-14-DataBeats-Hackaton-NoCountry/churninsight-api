package developer.ezandro.churninsigh.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PredictionResponse(
        @JsonProperty(value = "previsao")
        String prediction,

        @JsonProperty(value = "probabilidade")
        Double probability
) {}