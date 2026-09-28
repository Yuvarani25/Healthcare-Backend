package com.healthcare.vitalsservice.controller;

import com.healthcare.vitalsservice.dto.VitalRequest;
import com.healthcare.vitalsservice.dto.VitalResponse;
import com.healthcare.vitalsservice.service.VitalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/vitals")
@RequiredArgsConstructor
public class VitalController {

    private final VitalService vitalService;

    @PostMapping
    public ResponseEntity<VitalResponse> recordVital(@Valid @RequestBody VitalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vitalService.recordVital(request));
    }

    @GetMapping
    public ResponseEntity<List<VitalResponse>> getAllVitals() {
        return ResponseEntity.ok(vitalService.getAllVitals());
    }

    @GetMapping("/{vitalId}")
    public ResponseEntity<VitalResponse> getVitalById(@PathVariable Long vitalId) {
        return ResponseEntity.ok(vitalService.getVitalById(vitalId));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<VitalResponse>> getVitalsByPatient(
            @PathVariable Long patientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        if (from != null && to != null) {
            return ResponseEntity.ok(vitalService.getVitalsByPatientAndRange(patientId, from, to));
        }
        return ResponseEntity.ok(vitalService.getVitalsByPatient(patientId));
    }

    @GetMapping("/patient/{patientId}/latest")
    public ResponseEntity<VitalResponse> getLatestVital(@PathVariable Long patientId) {
        return ResponseEntity.ok(vitalService.getLatestVital(patientId));
    }

    @DeleteMapping("/{vitalId}")
    public ResponseEntity<Void> deleteVital(@PathVariable Long vitalId) {
        vitalService.deleteVital(vitalId);
        return ResponseEntity.noContent().build();
    }
}
