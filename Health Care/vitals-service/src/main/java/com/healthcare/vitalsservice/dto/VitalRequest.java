package com.healthcare.vitalsservice.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VitalRequest {

    @NotNull(message = "patientId is required")
    private Long patientId;

    @Min(value = 0, message = "heartRate must be >= 0")
    private Integer heartRate;

    @Pattern(regexp = "\\d{2,3}/\\d{2,3}", message = "bloodPressure must be in systolic/diastolic format, e.g. 120/80")
    private String bloodPressure;

    @DecimalMin(value = "0.0", message = "spo2 must be >= 0")
    @DecimalMax(value = "100.0", message = "spo2 must be <= 100")
    private Double spo2;

    private Double temperature;

    private LocalDateTime timestamp;
}
