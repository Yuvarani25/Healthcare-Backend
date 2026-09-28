package com.healthcare.alertservice.service;

import com.healthcare.alertservice.audit.AuditLogger;
import com.healthcare.alertservice.dto.AlertRequest;
import com.healthcare.alertservice.dto.AlertResponse;
import com.healthcare.alertservice.dto.VitalEvaluationRequest;
import com.healthcare.alertservice.entity.Alert;
import com.healthcare.alertservice.entity.AlertRule;
import com.healthcare.alertservice.entity.AlertStatus;
import com.healthcare.alertservice.exception.ResourceNotFoundException;
import com.healthcare.alertservice.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final AlertRuleService alertRuleService;
    private final AuditLogger auditLogger;

    @Transactional
    public AlertResponse createAlert(AlertRequest request) {
        Alert alert = Alert.builder()
                .patientId(request.getPatientId())
                .vitalType(request.getVitalType())
                .actualValue(request.getActualValue())
                .message(request.getMessage())
                .status(AlertStatus.NEW)
                .timestamp(request.getTimestamp())
                .build();
        AlertResponse response = toResponse(alertRepository.save(alert));
        auditLogger.record("ALERT_CREATED",
                "alertId=" + response.getAlertId() + " patientId=" + response.getPatientId());
        return response;
    }

    /** Checks a reading against its rule; returns the raised alert, or empty when the value is within range. */
    @Transactional
    public Optional<AlertResponse> evaluate(VitalEvaluationRequest request) {
        AlertRule rule = alertRuleService.findByVitalType(request.getVitalType())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No rule found for vital type: " + request.getVitalType()));

        double value = request.getValue();
        String breach = null;
        if (rule.getMinValue() != null && value < rule.getMinValue()) {
            breach = "below minimum " + rule.getMinValue();
        } else if (rule.getMaxValue() != null && value > rule.getMaxValue()) {
            breach = "above maximum " + rule.getMaxValue();
        }

        if (breach == null) {
            return Optional.empty();
        }

        Alert alert = Alert.builder()
                .patientId(request.getPatientId())
                .vitalType(rule.getVitalType())
                .actualValue(value)
                .message(String.format("%s severity: %s reading %s is %s",
                        rule.getSeverity(), rule.getVitalType(), value, breach))
                .status(AlertStatus.NEW)
                .timestamp(request.getTimestamp())
                .build();
        AlertResponse response = toResponse(alertRepository.save(alert));
        auditLogger.record("ALERT_RAISED", "alertId=" + response.getAlertId()
                + " patientId=" + response.getPatientId()
                + " vitalType=" + response.getVitalType()
                + " value=" + response.getActualValue());
        return Optional.of(response);
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> getAllAlerts(AlertStatus status) {
        List<Alert> alerts = status == null
                ? alertRepository.findAll()
                : alertRepository.findByStatusOrderByTimestampDesc(status);
        return alerts.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AlertResponse getAlertById(Long alertId) {
        return toResponse(findAlertOrThrow(alertId));
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> getAlertsByPatient(Long patientId, AlertStatus status) {
        List<Alert> alerts = status == null
                ? alertRepository.findByPatientIdOrderByTimestampDesc(patientId)
                : alertRepository.findByPatientIdAndStatusOrderByTimestampDesc(patientId, status);
        return alerts.stream().map(this::toResponse).toList();
    }

    @Transactional
    public AlertResponse updateStatus(Long alertId, AlertStatus status) {
        Alert alert = findAlertOrThrow(alertId);
        alert.setStatus(status);
        AlertResponse response = toResponse(alertRepository.save(alert));
        auditLogger.record("ALERT_STATUS_CHANGED", "alertId=" + alertId + " status=" + status);
        return response;
    }

    @Transactional
    public void deleteAlert(Long alertId) {
        alertRepository.delete(findAlertOrThrow(alertId));
        auditLogger.record("ALERT_DELETED", "alertId=" + alertId);
    }

    private Alert findAlertOrThrow(Long alertId) {
        return alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + alertId));
    }

    private AlertResponse toResponse(Alert alert) {
        return AlertResponse.builder()
                .alertId(alert.getAlertId())
                .patientId(alert.getPatientId())
                .vitalType(alert.getVitalType())
                .actualValue(alert.getActualValue())
                .message(alert.getMessage())
                .status(alert.getStatus())
                .timestamp(alert.getTimestamp())
                .build();
    }
}
