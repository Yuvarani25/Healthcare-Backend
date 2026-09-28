package com.healthcare.vitalsservice.service;

import com.healthcare.vitalsservice.audit.AuditLogger;
import com.healthcare.vitalsservice.dto.VitalRequest;
import com.healthcare.vitalsservice.dto.VitalResponse;
import com.healthcare.vitalsservice.entity.Patient;
import com.healthcare.vitalsservice.entity.Vital;
import com.healthcare.vitalsservice.exception.ResourceNotFoundException;
import com.healthcare.vitalsservice.repository.VitalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VitalServiceTest {

    @Mock
    private VitalRepository vitalRepository;

    @Mock
    private PatientService patientService;

    @Mock
    private AuditLogger auditLogger;

    @Mock
    private AlertDispatcher alertDispatcher;

    @InjectMocks
    private VitalService vitalService;

    private final Patient patient = Patient.builder().patientId(1L).name("Jane Doe").build();

    private Vital sampleVital() {
        return Vital.builder()
                .vitalId(10L)
                .patient(patient)
                .heartRate(82)
                .bloodPressure("120/80")
                .spo2(97.5)
                .temperature(36.9)
                .timestamp(LocalDateTime.of(2026, 1, 5, 10, 0))
                .build();
    }

    @Test
    void recordVitalResolvesPatientAndAudits() {
        when(patientService.findPatientOrThrow(1L)).thenReturn(patient);
        when(vitalRepository.save(any(Vital.class))).thenReturn(sampleVital());

        VitalRequest request = new VitalRequest(1L, 82, "120/80", 97.5, 36.9, null);
        VitalResponse response = vitalService.recordVital(request);

        assertThat(response.getVitalId()).isEqualTo(10L);
        assertThat(response.getPatientId()).isEqualTo(1L);
        assertThat(response.getBloodPressure()).isEqualTo("120/80");
        verify(auditLogger).record("VITAL_RECORDED", "patientId=1 vitalId=10");
        verify(alertDispatcher).dispatch(any(Vital.class), org.mockito.ArgumentMatchers.isNull());
    }

    @Test
    void getVitalsByPatientValidatesPatientExists() {
        when(patientService.findPatientOrThrow(1L)).thenReturn(patient);
        when(vitalRepository.findByPatientPatientIdOrderByTimestampDesc(1L))
                .thenReturn(List.of(sampleVital()));

        List<VitalResponse> responses = vitalService.getVitalsByPatient(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getSpo2()).isEqualTo(97.5);
    }

    @Test
    void getLatestVitalThrowsWhenPatientHasNoReadings() {
        when(patientService.findPatientOrThrow(1L)).thenReturn(patient);
        when(vitalRepository.findFirstByPatientPatientIdOrderByTimestampDesc(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> vitalService.getLatestVital(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No vitals");
    }

    @Test
    void deleteVitalThrowsWhenMissing() {
        when(vitalRepository.findById(55L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vitalService.deleteVital(55L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
