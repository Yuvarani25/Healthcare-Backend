package com.healthcare.alertservice.dto;

import com.healthcare.alertservice.entity.Severity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertRuleResponse {

    private Long ruleId;
    private String vitalType;
    private Double minValue;
    private Double maxValue;
    private Severity severity;
}
