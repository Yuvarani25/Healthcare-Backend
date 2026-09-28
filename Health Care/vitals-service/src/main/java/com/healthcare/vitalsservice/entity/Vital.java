package com.healthcare.vitalsservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "vitals", indexes = @Index(name = "idx_vitals_patient", columnList = "patient_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vital_id")
    private Long vitalId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "heart_rate")
    private Integer heartRate;

    @Column(name = "blood_pressure", length = 20)
    private String bloodPressure;

    @Column(name = "spo2")
    private Double spo2;

    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    void applyDefaults() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
