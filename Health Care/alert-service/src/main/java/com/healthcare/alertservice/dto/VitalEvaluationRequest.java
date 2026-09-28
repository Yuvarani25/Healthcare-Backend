package com.healthcare.alertservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Reading pushed from vitals-service to be checked against the alert rules. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VitalEvaluationRequest {

    @NotNull(message = "patientId is required")
    private Long patientId;

    @NotBlank(message = "vitalType is required")
    private String vitalType;

    @NotNull(message = "value is required")
    private Double value;

    private LocalDateTime timestamp;
}
