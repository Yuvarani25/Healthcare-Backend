package com.healthcare.analyticsgateway.controller;

import com.healthcare.analyticsgateway.dto.PatientTrendResponse;
import com.healthcare.analyticsgateway.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/patients/{patientId}/trends")
    public ResponseEntity<PatientTrendResponse> getTrends(@PathVariable Long patientId,
                                                          @RequestParam(defaultValue = "24") int windowHours) {
        return ResponseEntity.ok(analyticsService.getTrends(patientId, windowHours));
    }

    @GetMapping(value = "/patients/{patientId}/chart", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getChart(@PathVariable Long patientId,
                                           @RequestParam(defaultValue = "24") int windowHours) {
        return ResponseEntity.ok(analyticsService.getChart(patientId, windowHours));
    }
}
