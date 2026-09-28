package com.healthcare.alertservice.service;

import com.healthcare.alertservice.audit.AuditLogger;
import com.healthcare.alertservice.dto.AlertRuleRequest;
import com.healthcare.alertservice.dto.AlertRuleResponse;
import com.healthcare.alertservice.entity.AlertRule;
import com.healthcare.alertservice.exception.DuplicateResourceException;
import com.healthcare.alertservice.exception.ResourceNotFoundException;
import com.healthcare.alertservice.repository.AlertRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlertRuleService {

    private final AlertRuleRepository alertRuleRepository;
    private final AuditLogger auditLogger;

    @Transactional
    public AlertRuleResponse createRule(AlertRuleRequest request) {
        validateRange(request);
        if (alertRuleRepository.existsByVitalTypeIgnoreCase(request.getVitalType())) {
            throw new DuplicateResourceException("A rule already exists for vital type: " + request.getVitalType());
        }
        AlertRule rule = AlertRule.builder()
                .vitalType(request.getVitalType())
                .minValue(request.getMinValue())
                .maxValue(request.getMaxValue())
                .severity(request.getSeverity())
                .build();
        AlertRuleResponse response = toResponse(alertRuleRepository.save(rule));
        auditLogger.record("RULE_CREATED", "ruleId=" + response.getRuleId() + " vitalType=" + response.getVitalType());
        return response;
    }

    @Transactional(readOnly = true)
    public List<AlertRuleResponse> getAllRules() {
        return alertRuleRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AlertRuleResponse getRuleById(Long ruleId) {
        return toResponse(findRuleOrThrow(ruleId));
    }

    @Transactional(readOnly = true)
    public AlertRuleResponse getRuleByVitalType(String vitalType) {
        return toResponse(alertRuleRepository.findByVitalTypeIgnoreCase(vitalType)
                .orElseThrow(() -> new ResourceNotFoundException("No rule found for vital type: " + vitalType)));
    }

    @Transactional
    public AlertRuleResponse updateRule(Long ruleId, AlertRuleRequest request) {
        validateRange(request);
        AlertRule rule = findRuleOrThrow(ruleId);
        Optional<AlertRule> existing = alertRuleRepository.findByVitalTypeIgnoreCase(request.getVitalType());
        if (existing.isPresent() && !existing.get().getRuleId().equals(ruleId)) {
            throw new DuplicateResourceException("A rule already exists for vital type: " + request.getVitalType());
        }
        rule.setVitalType(request.getVitalType());
        rule.setMinValue(request.getMinValue());
        rule.setMaxValue(request.getMaxValue());
        rule.setSeverity(request.getSeverity());
        AlertRuleResponse response = toResponse(alertRuleRepository.save(rule));
        auditLogger.record("RULE_UPDATED", "ruleId=" + ruleId);
        return response;
    }

    @Transactional
    public void deleteRule(Long ruleId) {
        alertRuleRepository.delete(findRuleOrThrow(ruleId));
        auditLogger.record("RULE_DELETED", "ruleId=" + ruleId);
    }

    Optional<AlertRule> findByVitalType(String vitalType) {
        return alertRuleRepository.findByVitalTypeIgnoreCase(vitalType);
    }

    private AlertRule findRuleOrThrow(Long ruleId) {
        return alertRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert rule not found with id: " + ruleId));
    }

    private void validateRange(AlertRuleRequest request) {
        if (request.getMinValue() == null && request.getMaxValue() == null) {
            throw new IllegalArgumentException("At least one of minValue or maxValue must be provided");
        }
        if (request.getMinValue() != null && request.getMaxValue() != null
                && request.getMinValue() > request.getMaxValue()) {
            throw new IllegalArgumentException("minValue must be less than or equal to maxValue");
        }
    }

    private AlertRuleResponse toResponse(AlertRule rule) {
        return AlertRuleResponse.builder()
                .ruleId(rule.getRuleId())
                .vitalType(rule.getVitalType())
                .minValue(rule.getMinValue())
                .maxValue(rule.getMaxValue())
                .severity(rule.getSeverity())
                .build();
    }
}
