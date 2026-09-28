package com.healthcare.analyticsgateway.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MetricSummary(
        String metric,
        Integer count,
        Double average,
        Double minimum,
        Double maximum,
        Double latest,
        String trend) {
}
