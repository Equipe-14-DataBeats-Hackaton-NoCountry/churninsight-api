package developer.ezandro.churninsigh.exception;

import module java.base;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ApiErrorResponse(
        @JsonProperty(value = "status")
        int status,

        @JsonProperty(value = "erro")
        String error,

        @JsonProperty(value = "mensagens")
        List<String> messages
) {}