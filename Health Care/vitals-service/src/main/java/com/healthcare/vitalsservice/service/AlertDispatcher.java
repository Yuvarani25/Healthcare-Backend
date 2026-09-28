package com.healthcare.vitalsservice.service;

import com.healthcare.vitalsservice.client.AlertClient;
import com.healthcare.vitalsservice.client.VitalEvaluationRequest;
import com.healthcare.vitalsservice.entity.Vital;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/** Pushes each numeric reading to alert-service for threshold evaluation. */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlertDispatcher {

    private final AlertClient alertClient;

    @Async
    public void dispatch(Vital vital, String authorization) {
        if (authorization == null) {
            log.warn("Skipping alert evaluation: no caller credentials available");
            return;
        }
        Long patientId = vital.getPatient().getPatientId();
        for (Map.Entry<String, Double> metric : numericMetrics(vital).entrySet()) {
            VitalEvaluationRequest request = new VitalEvaluationRequest(
                    patientId, metric.getKey(), metric.getValue(), vital.getTimestamp());
            try {
                alertClient.evaluate(authorization, request);
            } catch (FeignException.NotFound ex) {
                log.debug("No alert rule configured for {}", metric.getKey());
            } catch (Exception ex) {
                log.warn("Alert evaluation failed for patient {} metric {}: {}",
                        patientId, metric.getKey(), ex.getMessage());
            }
        }
    }

    private Map<String, Double> numericMetrics(Vital vital) {
        Map<String, Double> metrics = new LinkedHashMap<>();
        if (vital.getHeartRate() != null) {
            metrics.put("heart_rate", vital.getHeartRate().doubleValue());
        }
        if (vital.getSpo2() != null) {
            metrics.put("spo2", vital.getSpo2());
        }
        if (vital.getTemperature() != null) {
            metrics.put("temperature", vital.getTemperature());
        }
        addBloodPressure(metrics, vital.getBloodPressure());
        return metrics;
    }

    private void addBloodPressure(Map<String, Double> metrics, String bloodPressure) {
        if (bloodPressure == null || !bloodPressure.contains("/")) {
            return;
        }
        String[] parts = bloodPressure.split("/", 2);
        try {
            metrics.put("systolic_bp", Double.parseDouble(parts[0].trim()));
            metrics.put("diastolic_bp", Double.parseDouble(parts[1].trim()));
        } catch (NumberFormatException ex) {
            log.warn("Unparseable blood pressure value: {}", bloodPressure);
        }
    }
}
