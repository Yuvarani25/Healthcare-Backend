package com.healthcare.vitalsservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientRequest {

    @NotBlank(message = "name is required")
    private String name;

    @NotNull(message = "age is required")
    @Min(value = 0, message = "age must be >= 0")
    @Max(value = 130, message = "age must be <= 130")
    private Integer age;

    @NotBlank(message = "gender is required")
    private String gender;

    private String bloodGroup;

    private String roomNo;

    private LocalDate admissionDate;
}
