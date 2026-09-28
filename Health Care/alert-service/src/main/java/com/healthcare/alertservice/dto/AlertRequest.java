package com.healthcare.alertservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlertRequest {

    @NotNull(message = "patientId is required")
    private Long patientId;

    @NotBlank(message = "vitalType is required")
    private String vitalType;

    @NotNull(message = "actualValue is required")
    private Double actualValue;

    private String message;

    private LocalDateTime timestamp;
}
