package developer.ezandro.churninsigh.exception;

import module java.base;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ApiErrorResponse(
        @JsonProperty("status")
        int status,

        @JsonProperty("erro")
        String error,

        @JsonProperty("mensagens")
        List<String> messages
) {}