package com.hackathon.databeats.churninsight.infra.exception;

import java.time.Instant;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String path
) {}