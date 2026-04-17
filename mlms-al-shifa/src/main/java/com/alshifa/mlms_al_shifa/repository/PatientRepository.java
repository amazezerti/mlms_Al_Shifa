package com.alshifa.mlms_al_shifa.repository;

import com.alshifa.mlms_al_shifa.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository
        extends JpaRepository<Patient, Long> {

    Optional<Patient> findByPatientCode(String patientCode);
    boolean existsByPatientCode(String patientCode);

    @Query("SELECT p FROM Patient p ORDER BY p.registeredAt DESC")
    List<Patient> findAllOrderByRegisteredAtDesc();

    @Query("""
        SELECT p FROM Patient p
        WHERE LOWER(p.fullName) LIKE LOWER(CONCAT('%', :q, '%'))
        OR LOWER(p.patientCode) LIKE LOWER(CONCAT('%', :q, '%'))
        ORDER BY p.registeredAt DESC
        """)
    List<Patient> search(
            @org.springframework.data.repository.query.Param("q") String query);
}