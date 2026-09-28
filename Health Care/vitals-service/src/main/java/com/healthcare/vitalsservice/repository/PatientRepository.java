package com.healthcare.vitalsservice.repository;

import com.healthcare.vitalsservice.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    List<Patient> findByRoomNo(String roomNo);
}
