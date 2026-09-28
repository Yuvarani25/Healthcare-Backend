package com.healthcare.vitalsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VitalResponse {

    private Long vitalId;
    private Long patientId;
    private Integer heartRate;
    private String bloodPressure;
    private Double spo2;
    private Double temperature;
    private LocalDateTime timestamp;
}
