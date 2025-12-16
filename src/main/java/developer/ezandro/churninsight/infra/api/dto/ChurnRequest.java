package developer.ezandro.churninsight.infra.api.dto;

import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ChurnRequest(

        @NotBlank
        String gender,

        @NotNull
        @Min(0)
        Integer age,

        @NotBlank
        String country,

        @JsonProperty("subscription_type")
        @NotBlank
        String subscriptionType,

        @JsonProperty("listening_time")
        @NotNull
        @PositiveOrZero
        Double listeningTime,

        @JsonProperty("songs_played_per_day")
        @NotNull
        @Min(0)
        Integer songsPlayedPerDay,

        @JsonProperty("skip_rate")
        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("1.0")
        Double skipRate,

        @JsonProperty("device_type")
        @NotBlank
        String deviceType,

        @JsonProperty("offline_listening")
        @NotNull
        Boolean offlineListening
) {}
