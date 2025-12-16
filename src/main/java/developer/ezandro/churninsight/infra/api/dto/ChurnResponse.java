package developer.ezandro.churninsight.infra.api.dto;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ChurnResponse(

        @JsonProperty("prediction_id")
        UUID predictionId,

        @JsonProperty("predicted_churn")
        String predictedChurn,

        Double probability
) {}
