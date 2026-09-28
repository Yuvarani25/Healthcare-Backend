package com.healthcare.vitalsservice.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VitalEvaluationRequest {

    private Long patientId;
    private String vitalType;
    private Double value;
    private LocalDateTime timestamp;
}
