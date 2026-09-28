package com.healthcare.vitalsservice.service;

import com.healthcare.vitalsservice.audit.AuditLogger;
import com.healthcare.vitalsservice.dto.PatientRequest;
import com.healthcare.vitalsservice.dto.PatientResponse;
import com.healthcare.vitalsservice.entity.Patient;
import com.healthcare.vitalsservice.exception.ResourceNotFoundException;
import com.healthcare.vitalsservice.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final AuditLogger auditLogger;

    @Transactional
    public PatientResponse createPatient(PatientRequest request) {
        Patient patient = Patient.builder()
                .name(request.getName())
                .age(request.getAge())
                .gender(request.getGender())
                .bloodGroup(request.getBloodGroup())
                .roomNo(request.getRoomNo())
                .admissionDate(request.getAdmissionDate())
                .build();
        PatientResponse response = toResponse(patientRepository.save(patient));
        auditLogger.record("PATIENT_CREATED", "patientId=" + response.getPatientId());
        return response;
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> getAllPatients() {
        return patientRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PatientResponse getPatientById(Long patientId) {
        return toResponse(findPatientOrThrow(patientId));
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> getPatientsByRoom(String roomNo) {
        return patientRepository.findByRoomNo(roomNo).stream().map(this::toResponse).toList();
    }

    @Transactional
    public PatientResponse updatePatient(Long patientId, PatientRequest request) {
        Patient patient = findPatientOrThrow(patientId);
        patient.setName(request.getName());
        patient.setAge(request.getAge());
        patient.setGender(request.getGender());
        patient.setBloodGroup(request.getBloodGroup());
        patient.setRoomNo(request.getRoomNo());
        if (request.getAdmissionDate() != null) {
            patient.setAdmissionDate(request.getAdmissionDate());
        }
        PatientResponse response = toResponse(patientRepository.save(patient));
        auditLogger.record("PATIENT_UPDATED", "patientId=" + patientId);
        return response;
    }

    @Transactional
    public void deletePatient(Long patientId) {
        patientRepository.delete(findPatientOrThrow(patientId));
        auditLogger.record("PATIENT_DELETED", "patientId=" + patientId);
    }

    Patient findPatientOrThrow(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));
    }

    private PatientResponse toResponse(Patient patient) {
        return PatientResponse.builder()
                .patientId(patient.getPatientId())
                .name(patient.getName())
                .age(patient.getAge())
                .gender(patient.getGender())
                .bloodGroup(patient.getBloodGroup())
                .roomNo(patient.getRoomNo())
                .admissionDate(patient.getAdmissionDate())
                .build();
    }
}
