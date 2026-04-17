package com.alshifa.mlms_al_shifa.service;

import com.alshifa.mlms_al_shifa.model.*;
import com.alshifa.mlms_al_shifa.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientAssignmentService {

    private final PatientAssignmentRepository assignmentRepository;
    private final PatientRepository           patientRepository;
    private final DepartmentRepository        departmentRepository;
    private final DoctorProfileRepository     doctorProfileRepository;
    private final UserRepository              userRepository;

    /**
     * Assign a patient to the least-loaded doctor
     * in the given department (load balancing).
     */
    @Transactional
    public PatientAssignment assignToLeastLoadedDoctor(
            Long patientId,
            Long departmentId,
            Long assignedByUserId,
            String notes) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Department dept = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        // Get doctors in this department
        List<DoctorProfile> doctors =
                doctorProfileRepository.findByDepartmentAndActiveTrue(dept);

        if (doctors.isEmpty())
            throw new RuntimeException(
                    "No active doctors in department: " + dept.getName());

        // Find least loaded doctor
        List<User> leastLoaded =
                assignmentRepository.findLeastLoadedDoctorInDepartment(departmentId);

        User assignedDoctor;
        if (leastLoaded.isEmpty()) {
            // No assignments yet — pick first doctor
            assignedDoctor = doctors.get(0).getUser();
        } else {
            assignedDoctor = leastLoaded.get(0);
        }

        User assignedBy = userRepository.findById(assignedByUserId)
                .orElse(null);

        PatientAssignment assignment = PatientAssignment.builder()
                .patient(patient)
                .doctor(assignedDoctor)
                .department(dept)
                .assignedBy(assignedBy)
                .status("ACTIVE")
                .notes(notes)
                .build();

        log.info("Patient {} assigned to doctor {} in {}",
                patient.getFullName(),
                assignedDoctor.getFullName(),
                dept.getName());

        return assignmentRepository.save(assignment);
    }

    public List<PatientAssignment> getAssignmentsForDoctor(User doctor) {
        return assignmentRepository.findByDoctorAndStatus(doctor, "ACTIVE");
    }

    public List<PatientAssignment> getAssignmentsForPatient(Long patientId) {
        return assignmentRepository.findByPatientId(patientId);
    }
}