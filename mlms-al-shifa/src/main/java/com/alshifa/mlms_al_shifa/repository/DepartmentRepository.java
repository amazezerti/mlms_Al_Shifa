package com.alshifa.mlms_al_shifa.repository;

import com.alshifa.mlms_al_shifa.model.Department;
import org.springframework.data.jpa.repository
        .JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository
        extends JpaRepository<Department, Long> {

    List<Department> findByActiveTrue();

    boolean existsByNameIgnoreCase(String name);
}