package com.healthcare.vitalsservice.service;

import com.healthcare.vitalsservice.audit.AuditLogger;
import com.healthcare.vitalsservice.dto.VitalRequest;
import com.healthcare.vitalsservice.dto.VitalResponse;
import com.healthcare.vitalsservice.entity.Patient;
import com.healthcare.vitalsservice.entity.Vital;
import com.healthcare.vitalsservice.exception.ResourceNotFoundException;
import com.healthcare.vitalsservice.repository.VitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VitalService {

    private final VitalRepository vitalRepository;
    private final PatientService patientService;
    private final AuditLogger auditLogger;
    private final AlertDispatcher alertDispatcher;

    @Transactional
    public VitalResponse recordVital(VitalRequest request) {
        Patient patient = patientService.findPatientOrThrow(request.getPatientId());
        Vital vital = Vital.builder()
                .patient(patient)
                .heartRate(request.getHeartRate())
                .bloodPressure(request.getBloodPressure())
                .spo2(request.getSpo2())
                .temperature(request.getTemperature())
                .timestamp(request.getTimestamp())
                .build();
        Vital saved = vitalRepository.save(vital);
        VitalResponse response = toResponse(saved);
        auditLogger.record("VITAL_RECORDED",
                "patientId=" + request.getPatientId() + " vitalId=" + response.getVitalId());
        alertDispatcher.dispatch(saved, currentAuthorizationHeader());
        return response;
    }

    /** Read on the request thread because the dispatch itself runs asynchronously. */
    private String currentAuthorizationHeader() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
        }
        return null;
    }

    @Transactional(readOnly = true)
    public List<VitalResponse> getAllVitals() {
        return vitalRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public VitalResponse getVitalById(Long vitalId) {
        return toResponse(vitalRepository.findById(vitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Vital not found with id: " + vitalId)));
    }

    @Transactional(readOnly = true)
    public List<VitalResponse> getVitalsByPatient(Long patientId) {
        patientService.findPatientOrThrow(patientId);
        return vitalRepository.findByPatientPatientIdOrderByTimestampDesc(patientId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public VitalResponse getLatestVital(Long patientId) {
        patientService.findPatientOrThrow(patientId);
        return toResponse(vitalRepository.findFirstByPatientPatientIdOrderByTimestampDesc(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("No vitals recorded for patient id: " + patientId)));
    }

    @Transactional(readOnly = true)
    public List<VitalResponse> getVitalsByPatientAndRange(Long patientId, LocalDateTime from, LocalDateTime to) {
        patientService.findPatientOrThrow(patientId);
        return vitalRepository.findByPatientPatientIdAndTimestampBetweenOrderByTimestampDesc(patientId, from, to)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public void deleteVital(Long vitalId) {
        Vital vital = vitalRepository.findById(vitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Vital not found with id: " + vitalId));
        vitalRepository.delete(vital);
        auditLogger.record("VITAL_DELETED", "vitalId=" + vitalId);
    }

    private VitalResponse toResponse(Vital vital) {
        return VitalResponse.builder()
                .vitalId(vital.getVitalId())
                .patientId(vital.getPatient().getPatientId())
                .heartRate(vital.getHeartRate())
                .bloodPressure(vital.getBloodPressure())
                .spo2(vital.getSpo2())
                .temperature(vital.getTemperature())
                .timestamp(vital.getTimestamp())
                .build();
    }
}
