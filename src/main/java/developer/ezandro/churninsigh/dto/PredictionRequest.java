package developer.ezandro.churninsigh.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record PredictionRequest(
        @JsonProperty(value = "tempo_contrato_meses")
        @NotNull
        @Min(0)
        Integer contractDurationMonths,

        @JsonProperty(value = "atrasos_pagamento")
        @NotNull
        @PositiveOrZero
        Integer paymentDelays,

        @JsonProperty(value = "uso_mensal")
        @NotNull
        @PositiveOrZero
        Double monthlyUsage,

        @JsonProperty(value = "plano")
        @NotBlank
        String plan
) {}