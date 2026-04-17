package com.alshifa.mlms_al_shifa.service;

import com.alshifa.mlms_al_shifa.model.Department;
import com.alshifa.mlms_al_shifa.repository
        .DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation
        .Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository
            departmentRepository;

    /**
     * Returns ALL departments regardless of active status.
     * Used by admin to see and manage everything.
     */
    public List<Department> getAll() {
        return departmentRepository.findAll();
    }

    /**
     * Returns only ACTIVE departments.
     * Used by receptionist, doctor, lab tech dropdowns.
     * This is what other portals see dynamically.
     */
    public List<Department> getAllActive() {
        return departmentRepository.findByActiveTrue();
    }

    public Department getById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Department not found: " + id));
    }

    @Transactional
    public Department create(
            String name, String description) {
        if (departmentRepository
                .existsByNameIgnoreCase(name)) {
            throw new RuntimeException(
                    "Department '" + name
                            + "' already exists.");
        }
        Department dept = Department.builder()
                .name(name)
                .description(description)
                .active(true)
                .build();
        return departmentRepository.save(dept);
    }

    @Transactional
    public void toggleActive(Long id) {
        Department dept = getById(id);
        dept.setActive(!dept.isActive());
        departmentRepository.save(dept);
    }

    @Transactional
    public void delete(Long id) {
        departmentRepository.deleteById(id);
    }
}