package com.healthcare.alertservice.bootstrap;

import com.healthcare.alertservice.entity.AlertRule;
import com.healthcare.alertservice.entity.Severity;
import com.healthcare.alertservice.repository.AlertRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/** Seeds clinically conventional adult thresholds so alerting works out of the box. */
@Configuration
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class AlertRuleSeeder {

    private static final List<AlertRule> DEFAULT_RULES = List.of(
            rule("heart_rate", 50.0, 120.0, Severity.HIGH),
            rule("spo2", 92.0, null, Severity.CRITICAL),
            rule("temperature", 35.0, 38.0, Severity.MEDIUM),
            rule("systolic_bp", 90.0, 140.0, Severity.HIGH),
            rule("diastolic_bp", 60.0, 90.0, Severity.MEDIUM));

    private static AlertRule rule(String vitalType, Double min, Double max, Severity severity) {
        return AlertRule.builder()
                .vitalType(vitalType)
                .minValue(min)
                .maxValue(max)
                .severity(severity)
                .build();
    }

    @Bean
    public ApplicationRunner seedAlertRules(AlertRuleRepository alertRuleRepository) {
        return args -> DEFAULT_RULES.stream()
                .filter(rule -> !alertRuleRepository.existsByVitalTypeIgnoreCase(rule.getVitalType()))
                .forEach(rule -> {
                    alertRuleRepository.save(rule);
                    log.info("Seeded default alert rule for {}", rule.getVitalType());
                });
    }
}
