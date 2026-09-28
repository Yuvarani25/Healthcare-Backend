package com.healthcare.analyticsgateway.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PatientTrendResponse(
        Long patientId,
        Integer windowHours,
        Integer sampleCount,
        OffsetDateTime fromTime,
        OffsetDateTime toTime,
        List<MetricSummary> metrics) {
}
