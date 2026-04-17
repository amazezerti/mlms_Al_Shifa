package com.alshifa.mlms_al_shifa.repository;

import com.alshifa.mlms_al_shifa.model.Department;
import com.alshifa.mlms_al_shifa.model.DoctorProfile;
import com.alshifa.mlms_al_shifa.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorProfileRepository
        extends JpaRepository<DoctorProfile, Long> {

    Optional<DoctorProfile> findByUser(User user);
    Optional<DoctorProfile> findByUserId(Long userId);
    List<DoctorProfile> findByDepartmentAndActiveTrue(Department department);
    List<DoctorProfile> findByActiveTrue();
}