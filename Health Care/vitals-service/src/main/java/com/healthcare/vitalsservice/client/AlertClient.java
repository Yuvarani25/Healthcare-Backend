package com.healthcare.vitalsservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "alert-service")
public interface AlertClient {

    @PostMapping(value = "/api/alerts/evaluate", consumes = MediaType.APPLICATION_JSON_VALUE)
    void evaluate(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                  @RequestBody VitalEvaluationRequest request);
}
