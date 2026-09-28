package com.healthcare.alertservice.dto;

import com.healthcare.alertservice.entity.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlertRuleRequest {

    @NotBlank(message = "vitalType is required")
    private String vitalType;

    private Double minValue;

    private Double maxValue;

    @NotNull(message = "severity is required")
    private Severity severity;
}
