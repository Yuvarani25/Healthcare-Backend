package com.healthcare.alertservice.controller;

import com.healthcare.alertservice.dto.AlertRequest;
import com.healthcare.alertservice.dto.AlertResponse;
import com.healthcare.alertservice.dto.VitalEvaluationRequest;
import com.healthcare.alertservice.entity.AlertStatus;
import com.healthcare.alertservice.service.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @PostMapping
    public ResponseEntity<AlertResponse> createAlert(@Valid @RequestBody AlertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(alertService.createAlert(request));
    }

    // Returns 201 with the raised alert, or 204 when the reading is within the configured range.
    @PostMapping("/evaluate")
    public ResponseEntity<AlertResponse> evaluate(@Valid @RequestBody VitalEvaluationRequest request) {
        return alertService.evaluate(request)
                .map(alert -> ResponseEntity.status(HttpStatus.CREATED).body(alert))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping
    public ResponseEntity<List<AlertResponse>> getAllAlerts(@RequestParam(required = false) AlertStatus status) {
        return ResponseEntity.ok(alertService.getAllAlerts(status));
    }

    @GetMapping("/{alertId}")
    public ResponseEntity<AlertResponse> getAlertById(@PathVariable Long alertId) {
        return ResponseEntity.ok(alertService.getAlertById(alertId));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AlertResponse>> getAlertsByPatient(@PathVariable Long patientId,
                                                                  @RequestParam(required = false) AlertStatus status) {
        return ResponseEntity.ok(alertService.getAlertsByPatient(patientId, status));
    }

    @PatchMapping("/{alertId}/status")
    public ResponseEntity<AlertResponse> updateStatus(@PathVariable Long alertId,
                                                      @RequestParam AlertStatus status) {
        return ResponseEntity.ok(alertService.updateStatus(alertId, status));
    }

    @DeleteMapping("/{alertId}")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long alertId) {
        alertService.deleteAlert(alertId);
        return ResponseEntity.noContent().build();
    }
}
