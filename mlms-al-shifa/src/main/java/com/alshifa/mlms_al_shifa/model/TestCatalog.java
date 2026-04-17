package com.alshifa.mlms_al_shifa.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "test_catalog")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "test_code", nullable = false,
            unique = true, length = 20)
    private String testCode;

    @Column(name = "test_name", nullable = false,
            length = 150)
    private String testName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "sample_type", length = 50)
    private String sampleType;

    @Column(name = "turnaround_hours", nullable = false)
    @Builder.Default
    private Integer turnaroundHours = 24;

    @Column(nullable = false,
            precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "normal_range", length = 100)
    private String normalRange;

    @Column(length = 30)
    private String unit;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}