package com.agentstudy.agent;

import org.springframework.http.HttpStatus;

public class LlmException extends RuntimeException {

    private final LlmErrorType errorType;
    private final HttpStatus status;

    public LlmException(LlmErrorType errorType, HttpStatus status, String message) {
        super(message);
        this.errorType = errorType;
        this.status = status;
    }

    public LlmErrorType getErrorType() {
        return errorType;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
