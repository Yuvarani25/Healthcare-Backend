package com.healthcare.vitalsservice.controller;

import com.healthcare.vitalsservice.dto.PatientRequest;
import com.healthcare.vitalsservice.dto.PatientResponse;
import com.healthcare.vitalsservice.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    public ResponseEntity<PatientResponse> createPatient(@Valid @RequestBody PatientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.createPatient(request));
    }

    @GetMapping
    public ResponseEntity<List<PatientResponse>> getAllPatients(@RequestParam(required = false) String roomNo) {
        return ResponseEntity.ok(roomNo == null
                ? patientService.getAllPatients()
                : patientService.getPatientsByRoom(roomNo));
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable Long patientId) {
        return ResponseEntity.ok(patientService.getPatientById(patientId));
    }

    @PutMapping("/{patientId}")
    public ResponseEntity<PatientResponse> updatePatient(@PathVariable Long patientId,
                                                         @Valid @RequestBody PatientRequest request) {
        return ResponseEntity.ok(patientService.updatePatient(patientId, request));
    }

    @DeleteMapping("/{patientId}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long patientId) {
        patientService.deletePatient(patientId);
        return ResponseEntity.noContent().build();
    }
}
