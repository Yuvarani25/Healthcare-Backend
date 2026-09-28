package com.healthcare.analyticsgateway.client;

import com.healthcare.analyticsgateway.exception.AnalyticsUnavailableException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class AnalyticsFeignConfig {

    @Bean
    public ErrorDecoder analyticsErrorDecoder() {
        return (methodKey, response) -> {
            HttpStatusCode status = HttpStatusCode.valueOf(response.status());
            if (status.is5xxServerError()) {
                status = HttpStatus.SERVICE_UNAVAILABLE;
            }
            return new AnalyticsUnavailableException(
                    "analytics-service returned " + response.status() + " for " + methodKey, status);
        };
    }
}
