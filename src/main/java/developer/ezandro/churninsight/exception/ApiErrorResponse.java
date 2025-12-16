package developer.ezandro.churninsight.exception;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ApiErrorResponse(
        @JsonProperty(value = "status")
        int status,

        @JsonProperty(value = "erro")
        String error,

        @JsonProperty(value = "mensagens")
        List<String> messages
) {}