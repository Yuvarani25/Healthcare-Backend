package com.healthcare.analyticsgateway.client;

import com.healthcare.analyticsgateway.dto.PatientTrendResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/** The FastAPI analytics service is not registered with Eureka, so it is addressed by URL. */
@FeignClient(
        name = "analytics-python",
        url = "${analytics.python.base-url}",
        configuration = AnalyticsFeignConfig.class)
public interface AnalyticsFeignClient {

    @GetMapping(value = "/api/analytics/patients/{patientId}/trends",
            produces = MediaType.APPLICATION_JSON_VALUE)
    PatientTrendResponse getTrends(@PathVariable("patientId") Long patientId,
                                   @RequestParam("windowHours") int windowHours);

    @GetMapping(value = "/api/analytics/patients/{patientId}/chart",
            produces = MediaType.IMAGE_PNG_VALUE)
    byte[] getChart(@PathVariable("patientId") Long patientId,
                    @RequestParam("windowHours") int windowHours);
}
