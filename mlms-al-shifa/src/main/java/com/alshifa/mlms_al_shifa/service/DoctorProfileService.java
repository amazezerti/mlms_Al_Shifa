package com.alshifa.mlms_al_shifa.service;

import com.alshifa.mlms_al_shifa.model.*;
import com.alshifa.mlms_al_shifa.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DoctorProfileService {

    private final DoctorProfileRepository
            doctorProfileRepository;
    private final DepartmentRepository
            departmentRepository;
    private final UserRepository
            userRepository;

    public List<DoctorProfile> getAllActiveDoctors() {
        return doctorProfileRepository.findByActiveTrue();
    }

    /**
     * Returns users with ROLE_DOCTOR — even without profiles.
     * Used by receptionist to see all doctors.
     */
    public List<User> getAllDoctorUsers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream()
                        .anyMatch(r -> r.getName()
                                .equals("ROLE_DOCTOR")))
                .toList();
    }

    public List<DoctorProfile> getDoctorsByDepartment(
            Long departmentId) {
        Department dept = departmentRepository
                .findById(departmentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Department not found"));
        return doctorProfileRepository
                .findByDepartmentAndActiveTrue(dept);
    }

    public Optional<DoctorProfile> getByUserId(Long userId) {
        return doctorProfileRepository.findByUserId(userId);
    }

    @Transactional
    public DoctorProfile createOrUpdate(
            Long userId,
            Long departmentId,
            String specialization,
            String qualification,
            String licenseNumber,
            String bio,
            Integer yearsExperience) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
        Department dept = departmentRepository
                .findById(departmentId)
                .orElseThrow(() ->
                        new RuntimeException("Department not found"));

        DoctorProfile profile = doctorProfileRepository
                .findByUserId(userId)
                .orElse(DoctorProfile.builder()
                        .user(user).build());

        profile.setDepartment(dept);
        profile.setSpecialization(specialization);
        profile.setQualification(qualification);
        profile.setLicenseNumber(licenseNumber);
        profile.setBio(bio);
        profile.setYearsExperience(yearsExperience);
        profile.setActive(true);

        return doctorProfileRepository.save(profile);
    }
}