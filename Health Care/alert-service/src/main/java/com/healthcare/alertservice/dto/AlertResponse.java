package com.healthcare.alertservice.dto;

import com.healthcare.alertservice.entity.AlertStatus;
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
public class AlertResponse {

    private Long alertId;
    private Long patientId;
    private String vitalType;
    private Double actualValue;
    private String message;
    private AlertStatus status;
    private LocalDateTime timestamp;
}
