package com.healthcare.alertservice.service;

import com.healthcare.alertservice.audit.AuditLogger;
import com.healthcare.alertservice.dto.AlertResponse;
import com.healthcare.alertservice.dto.VitalEvaluationRequest;
import com.healthcare.alertservice.entity.Alert;
import com.healthcare.alertservice.entity.AlertRule;
import com.healthcare.alertservice.entity.AlertStatus;
import com.healthcare.alertservice.entity.Severity;
import com.healthcare.alertservice.exception.ResourceNotFoundException;
import com.healthcare.alertservice.repository.AlertRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private AlertRuleService alertRuleService;

    @Mock
    private AuditLogger auditLogger;

    @InjectMocks
    private AlertService alertService;

    private final AlertRule heartRateRule = AlertRule.builder()
            .ruleId(1L)
            .vitalType("heart_rate")
            .minValue(50.0)
            .maxValue(120.0)
            .severity(Severity.HIGH)
            .build();

    private void stubSaveEchoingArgument() {
        when(alertRepository.save(any(Alert.class))).thenAnswer(inv -> {
            Alert alert = inv.getArgument(0);
            alert.setAlertId(100L);
            return alert;
        });
    }

    @Test
    void evaluateRaisesAlertWhenValueAboveMaximum() {
        when(alertRuleService.findByVitalType("heart_rate")).thenReturn(Optional.of(heartRateRule));
        stubSaveEchoingArgument();

        Optional<AlertResponse> result = alertService.evaluate(
                new VitalEvaluationRequest(7L, "heart_rate", 145.0, null));

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(AlertStatus.NEW);
        assertThat(result.get().getActualValue()).isEqualTo(145.0);
        assertThat(result.get().getMessage()).contains("above maximum 120.0");
    }

    @Test
    void evaluateRaisesAlertWhenValueBelowMinimum() {
        when(alertRuleService.findByVitalType("heart_rate")).thenReturn(Optional.of(heartRateRule));
        stubSaveEchoingArgument();

        Optional<AlertResponse> result = alertService.evaluate(
                new VitalEvaluationRequest(7L, "heart_rate", 42.0, null));

        assertThat(result).isPresent();
        assertThat(result.get().getMessage()).contains("below minimum 50.0");
    }

    @Test
    void evaluateReturnsEmptyWhenValueWithinRange() {
        when(alertRuleService.findByVitalType("heart_rate")).thenReturn(Optional.of(heartRateRule));

        Optional<AlertResponse> result = alertService.evaluate(
                new VitalEvaluationRequest(7L, "heart_rate", 80.0, null));

        assertThat(result).isEmpty();
        verify(alertRepository, never()).save(any(Alert.class));
    }

    @Test
    void evaluateThrowsWhenNoRuleConfigured() {
        when(alertRuleService.findByVitalType("temperature")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alertService.evaluate(
                new VitalEvaluationRequest(7L, "temperature", 39.5, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No rule found");
    }

    @Test
    void updateStatusPersistsNewStatus() {
        Alert existing = Alert.builder()
                .alertId(100L)
                .patientId(7L)
                .vitalType("heart_rate")
                .actualValue(145.0)
                .status(AlertStatus.NEW)
                .build();
        when(alertRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(alertRepository.save(any(Alert.class))).thenAnswer(inv -> inv.getArgument(0));

        AlertResponse response = alertService.updateStatus(100L, AlertStatus.ACKNOWLEDGED);

        assertThat(response.getStatus()).isEqualTo(AlertStatus.ACKNOWLEDGED);
        verify(auditLogger).record("ALERT_STATUS_CHANGED", "alertId=100 status=ACKNOWLEDGED");
    }
}
