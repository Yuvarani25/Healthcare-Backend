package com.healthcare.analyticsgateway.service;

import com.healthcare.analyticsgateway.client.AnalyticsFeignClient;
import com.healthcare.analyticsgateway.dto.PatientTrendResponse;
import com.healthcare.analyticsgateway.exception.AnalyticsUnavailableException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsFeignClient analyticsFeignClient;

    public PatientTrendResponse getTrends(Long patientId, int windowHours) {
        try {
            return analyticsFeignClient.getTrends(patientId, windowHours);
        } catch (FeignException ex) {
            throw new AnalyticsUnavailableException("analytics-service is unreachable", ex);
        }
    }

    public byte[] getChart(Long patientId, int windowHours) {
        try {
            return analyticsFeignClient.getChart(patientId, windowHours);
        } catch (FeignException ex) {
            throw new AnalyticsUnavailableException("analytics-service is unreachable", ex);
        }
    }
}
