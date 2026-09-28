package com.healthcare.alertservice.service;

import com.healthcare.alertservice.audit.AuditLogger;
import com.healthcare.alertservice.dto.AlertRuleRequest;
import com.healthcare.alertservice.dto.AlertRuleResponse;
import com.healthcare.alertservice.entity.AlertRule;
import com.healthcare.alertservice.entity.Severity;
import com.healthcare.alertservice.exception.DuplicateResourceException;
import com.healthcare.alertservice.exception.ResourceNotFoundException;
import com.healthcare.alertservice.repository.AlertRuleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertRuleServiceTest {

    @Mock
    private AlertRuleRepository alertRuleRepository;

    @Mock
    private AuditLogger auditLogger;

    @InjectMocks
    private AlertRuleService alertRuleService;

    private AlertRule savedRule() {
        return AlertRule.builder()
                .ruleId(1L)
                .vitalType("heart_rate")
                .minValue(50.0)
                .maxValue(120.0)
                .severity(Severity.HIGH)
                .build();
    }

    @Test
    void createRuleSavesWhenVitalTypeIsNew() {
        when(alertRuleRepository.existsByVitalTypeIgnoreCase("heart_rate")).thenReturn(false);
        when(alertRuleRepository.save(any(AlertRule.class))).thenReturn(savedRule());

        AlertRuleResponse response = alertRuleService.createRule(
                new AlertRuleRequest("heart_rate", 50.0, 120.0, Severity.HIGH));

        assertThat(response.getRuleId()).isEqualTo(1L);
        assertThat(response.getSeverity()).isEqualTo(Severity.HIGH);
    }

    @Test
    void createRuleRejectsDuplicateVitalType() {
        when(alertRuleRepository.existsByVitalTypeIgnoreCase("heart_rate")).thenReturn(true);

        assertThatThrownBy(() -> alertRuleService.createRule(
                new AlertRuleRequest("heart_rate", 50.0, 120.0, Severity.HIGH)))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void createRuleRejectsMissingBounds() {
        assertThatThrownBy(() -> alertRuleService.createRule(
                new AlertRuleRequest("spo2", null, null, Severity.CRITICAL)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minValue or maxValue");
    }

    @Test
    void createRuleRejectsInvertedBounds() {
        assertThatThrownBy(() -> alertRuleService.createRule(
                new AlertRuleRequest("spo2", 100.0, 90.0, Severity.CRITICAL)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("less than or equal");
    }

    @Test
    void getRuleByVitalTypeThrowsWhenAbsent() {
        when(alertRuleRepository.findByVitalTypeIgnoreCase(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alertRuleService.getRuleByVitalType("temperature"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
