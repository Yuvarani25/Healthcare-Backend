package com.healthcare.alertservice.repository;

import com.healthcare.alertservice.entity.AlertRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlertRuleRepository extends JpaRepository<AlertRule, Long> {

    Optional<AlertRule> findByVitalTypeIgnoreCase(String vitalType);

    boolean existsByVitalTypeIgnoreCase(String vitalType);
}
