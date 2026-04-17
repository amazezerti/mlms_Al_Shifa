package com.alshifa.mlms_al_shifa.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "doctor_profiles")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(length = 150)
    private String specialization;

    @Column(length = 200)
    private String qualification;

    @Column(name = "license_number", length = 80)
    private String licenseNumber;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "profile_pic", length = 255)
    private String profilePic;

    @Column(name = "years_experience")
    private Integer yearsExperience = 0;

    @Column(nullable = false)
    private boolean active = true;
}