package com.healthcare.vitalsservice.service;

import com.healthcare.vitalsservice.client.AlertClient;
import com.healthcare.vitalsservice.client.VitalEvaluationRequest;
import com.healthcare.vitalsservice.entity.Patient;
import com.healthcare.vitalsservice.entity.Vital;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AlertDispatcherTest {

    private static final String BEARER = "Bearer test-token";

    @Mock
    private AlertClient alertClient;

    @InjectMocks
    private AlertDispatcher alertDispatcher;

    private Vital vital(Integer heartRate, String bloodPressure, Double spo2, Double temperature) {
        return Vital.builder()
                .vitalId(1L)
                .patient(Patient.builder().patientId(9L).build())
                .heartRate(heartRate)
                .bloodPressure(bloodPressure)
                .spo2(spo2)
                .temperature(temperature)
                .timestamp(LocalDateTime.of(2026, 1, 5, 10, 0))
                .build();
    }

    @Test
    void dispatchSendsOneRequestPerPopulatedMetric() {
        alertDispatcher.dispatch(vital(88, "130/85", 96.0, 37.2), BEARER);

        ArgumentCaptor<VitalEvaluationRequest> captor =
                ArgumentCaptor.forClass(VitalEvaluationRequest.class);
        verify(alertClient, org.mockito.Mockito.times(5)).evaluate(eq(BEARER), captor.capture());

        List<VitalEvaluationRequest> sent = captor.getAllValues();
        assertThat(sent).extracting(VitalEvaluationRequest::getVitalType)
                .containsExactly("heart_rate", "spo2", "temperature", "systolic_bp", "diastolic_bp");
        assertThat(sent).allSatisfy(request -> assertThat(request.getPatientId()).isEqualTo(9L));
        assertThat(sent.get(3).getValue()).isEqualTo(130.0);
        assertThat(sent.get(4).getValue()).isEqualTo(85.0);
    }

    @Test
    void dispatchSkipsNullMetrics() {
        alertDispatcher.dispatch(vital(88, null, null, null), BEARER);

        verify(alertClient, org.mockito.Mockito.times(1))
                .evaluate(eq(BEARER), any(VitalEvaluationRequest.class));
    }

    @Test
    void dispatchWithoutCredentialsMakesNoDownstreamCall() {
        alertDispatcher.dispatch(vital(88, "120/80", 98.0, 36.6), null);

        verify(alertClient, never()).evaluate(anyString(), any(VitalEvaluationRequest.class));
    }

    @Test
    void dispatchIgnoresMissingRuleResponses() {
        doThrow(notFound()).when(alertClient)
                .evaluate(anyString(), any(VitalEvaluationRequest.class));

        assertThatCode(() -> alertDispatcher.dispatch(vital(88, null, null, null), BEARER))
                .doesNotThrowAnyException();
    }

    @Test
    void dispatchSwallowsDownstreamFailuresSoVitalRecordingIsNotLost() {
        doThrow(new RuntimeException("connection refused"))
                .when(alertClient).evaluate(anyString(), any(VitalEvaluationRequest.class));

        assertThatCode(() -> alertDispatcher.dispatch(vital(88, "120/80", 98.0, 36.6), BEARER))
                .doesNotThrowAnyException();
    }

    private FeignException.NotFound notFound() {
        Request request = Request.create(Request.HttpMethod.POST, "/api/alerts/evaluate",
                Collections.emptyMap(), new byte[0], StandardCharsets.UTF_8, new RequestTemplate());
        return new FeignException.NotFound("no rule", request, null, Collections.emptyMap());
    }
}
