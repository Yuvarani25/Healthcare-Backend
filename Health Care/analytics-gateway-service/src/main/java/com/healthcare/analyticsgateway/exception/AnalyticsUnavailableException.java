package com.healthcare.analyticsgateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class AnalyticsUnavailableException extends RuntimeException {

    private final HttpStatusCode status;

    public AnalyticsUnavailableException(String message, HttpStatusCode status) {
        super(message);
        this.status = status;
    }

    public AnalyticsUnavailableException(String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.SERVICE_UNAVAILABLE;
    }

    public HttpStatusCode getStatus() {
        return status;
    }
}
