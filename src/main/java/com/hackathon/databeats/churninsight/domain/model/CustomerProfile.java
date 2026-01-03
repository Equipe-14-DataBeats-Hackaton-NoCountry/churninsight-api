package com.hackathon.databeats.churninsight.domain.model;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfile {
    @NotBlank(message = "Gender é obrigatório")
    private String gender;

    @NotNull(message = "Age é obrigatório")
    @Min(value = 10, message = "Idade mínima é 10 anos")
    @Max(value = 120, message = "Idade máxima é 120 anos")
    private Integer age;

    @NotBlank(message = "Country é obrigatório")
    private String country;

    @NotBlank(message = "SubscriptionType é obrigatório")
    private String subscriptionType;

    @NotNull(message = "ListeningTime é obrigatório")
    @Positive(message = "ListeningTime deve ser positivo")
    private Double listeningTime;

    @NotNull(message = "SongsPlayedPerDay é obrigatório")
    @Min(value = 0, message = "SongsPlayedPerDay não pode ser negativo")
    private Integer songsPlayedPerDay;

    @NotNull(message = "SkipRate é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "SkipRate deve ser >= 0")
    @DecimalMax(value = "1.0", inclusive = true, message = "SkipRate deve ser <= 1")
    private Double skipRate;

    @NotNull(message = "AdsListenedPerWeek é obrigatório")
    @Min(value = 0, message = "AdsListenedPerWeek não pode ser negativo")
    private Integer adsListenedPerWeek;

    @NotBlank(message = "DeviceType é obrigatório")
    private String deviceType;

    @NotNull(message = "OfflineListening é obrigatório")
    private Boolean offlineListening;

    @NotBlank(message = "UserId é obrigatório")
    private String userId;
}