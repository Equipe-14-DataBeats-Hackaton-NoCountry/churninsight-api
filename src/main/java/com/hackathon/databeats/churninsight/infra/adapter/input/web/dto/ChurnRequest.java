package com.hackathon.databeats.churninsight.infra.adapter.input.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

public record ChurnRequest(
        @NotBlank(message = "O gênero é obrigatório")
        String gender,

        @NotNull(message = "A idade é obrigatória")
        @Min(value = 0, message = "A idade não pode ser negativa")
        Integer age,

        @NotBlank(message = "O país é obrigatório")
        String country,

        @JsonProperty("subscription_type")
        @NotBlank(message = "O tipo de assinatura é obrigatório")
        String subscriptionType,

        @JsonProperty("listening_time")
        @NotNull(message = "O tempo de audição é obrigatório")
        @PositiveOrZero(message = "O tempo de audição deve ser positivo")
        Double listeningTime,

        @JsonProperty("songs_played_per_day")
        @NotNull(message = "A quantidade de músicas é obrigatória")
        @Min(value = 0, message = "A quantidade de músicas não pode ser negativa")
        Integer songsPlayedPerDay,

        @JsonProperty("skip_rate")
        @NotNull(message = "A taxa de pulos é obrigatória")
        @DecimalMin(value = "0.0", message = "A taxa de pulos mínima é 0.0")
        @DecimalMax(value = "1.0", message = "A taxa de pulos máxima é 1.0")
        Double skipRate,

        @JsonProperty("device_type")
        @NotBlank(message = "O tipo de dispositivo é obrigatório")
        String deviceType,

        @JsonProperty("offline_listening")
        @NotNull(message = "A informação de audição offline é obrigatória")
        Boolean offlineListening
) { }
