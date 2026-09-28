package com.healthcare.alertservice.repository;

import com.healthcare.alertservice.entity.Alert;
import com.healthcare.alertservice.entity.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByPatientIdOrderByTimestampDesc(Long patientId);

    List<Alert> findByStatusOrderByTimestampDesc(AlertStatus status);

    List<Alert> findByPatientIdAndStatusOrderByTimestampDesc(Long patientId, AlertStatus status);
}
