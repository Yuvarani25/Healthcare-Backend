package com.healthcare.vitalsservice.repository;

import com.healthcare.vitalsservice.entity.Vital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VitalRepository extends JpaRepository<Vital, Long> {

    List<Vital> findByPatientPatientIdOrderByTimestampDesc(Long patientId);

    Optional<Vital> findFirstByPatientPatientIdOrderByTimestampDesc(Long patientId);

    List<Vital> findByPatientPatientIdAndTimestampBetweenOrderByTimestampDesc(
            Long patientId, LocalDateTime from, LocalDateTime to);
}
