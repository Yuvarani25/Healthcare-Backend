package com.healthcare.vitalsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientResponse {

    private Long patientId;
    private String name;
    private Integer age;
    private String gender;
    private String bloodGroup;
    private String roomNo;
    private LocalDate admissionDate;
}
