package com.alshifa.mlms_al_shifa.repository;

import com.alshifa.mlms_al_shifa.model.PatientAssignment;
import com.alshifa.mlms_al_shifa.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PatientAssignmentRepository
        extends JpaRepository<PatientAssignment, Long> {

    List<PatientAssignment> findByDoctorAndStatus(User doctor, String status);

    List<PatientAssignment> findByPatientId(Long patientId);

    // Count patients per doctor in a department (for load balancing)
    @Query("""
        SELECT pa.doctor, COUNT(pa)
        FROM PatientAssignment pa
        WHERE pa.department.id = :deptId
        AND pa.status = 'ACTIVE'
        GROUP BY pa.doctor
        ORDER BY COUNT(pa) ASC
        """)
    List<Object[]> findDoctorLoadByDepartment(
            @Param("deptId") Long departmentId);

    @Query("""
        SELECT pa.doctor
        FROM PatientAssignment pa
        WHERE pa.department.id = :deptId
        AND pa.status = 'ACTIVE'
        GROUP BY pa.doctor
        ORDER BY COUNT(pa) ASC
        """)
    List<User> findLeastLoadedDoctorInDepartment(
            @Param("deptId") Long departmentId);
}