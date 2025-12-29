package com.hackathon.databeats.churninsight.infra.exception;

public class ModelInferenceException extends RuntimeException {
    public ModelInferenceException(String message) {
        super(message);
    }
}