package com.healthcare.vitalsservice.service;

import com.healthcare.vitalsservice.audit.AuditLogger;
import com.healthcare.vitalsservice.dto.PatientRequest;
import com.healthcare.vitalsservice.dto.PatientResponse;
import com.healthcare.vitalsservice.entity.Patient;
import com.healthcare.vitalsservice.exception.ResourceNotFoundException;
import com.healthcare.vitalsservice.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AuditLogger auditLogger;

    @InjectMocks
    private PatientService patientService;

    private Patient samplePatient() {
        return Patient.builder()
                .patientId(1L)
                .name("Jane Doe")
                .age(42)
                .gender("FEMALE")
                .bloodGroup("O+")
                .roomNo("ICU-3")
                .admissionDate(LocalDate.of(2026, 1, 5))
                .build();
    }

    @Test
    void createPatientPersistsAndAudits() {
        PatientRequest request = new PatientRequest("Jane Doe", 42, "FEMALE", "O+", "ICU-3", null);
        when(patientRepository.save(any(Patient.class))).thenReturn(samplePatient());

        PatientResponse response = patientService.createPatient(request);

        assertThat(response.getPatientId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Jane Doe");
        verify(auditLogger).record("PATIENT_CREATED", "patientId=1");
    }

    @Test
    void getPatientByIdReturnsMappedResponse() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(samplePatient()));

        PatientResponse response = patientService.getPatientById(1L);

        assertThat(response.getRoomNo()).isEqualTo("ICU-3");
        assertThat(response.getAdmissionDate()).isEqualTo(LocalDate.of(2026, 1, 5));
    }

    @Test
    void getPatientByIdThrowsWhenMissing() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.getPatientById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updatePatientKeepsExistingAdmissionDateWhenRequestOmitsIt() {
        Patient existing = samplePatient();
        when(patientRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        PatientRequest request = new PatientRequest("Jane Roe", 43, "FEMALE", "O+", "ICU-4", null);
        PatientResponse response = patientService.updatePatient(1L, request);

        assertThat(response.getName()).isEqualTo("Jane Roe");
        assertThat(response.getRoomNo()).isEqualTo("ICU-4");
        assertThat(response.getAdmissionDate()).isEqualTo(LocalDate.of(2026, 1, 5));
    }

    @Test
    void deletePatientThrowsWhenMissing() {
        when(patientRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.deletePatient(7L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
