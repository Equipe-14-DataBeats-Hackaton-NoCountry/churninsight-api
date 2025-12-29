package com.hackathon.databeats.churninsight.infra.exception;

public class MetadataLoadException extends RuntimeException {
    public MetadataLoadException(String message) {
        super(message);
    }
}