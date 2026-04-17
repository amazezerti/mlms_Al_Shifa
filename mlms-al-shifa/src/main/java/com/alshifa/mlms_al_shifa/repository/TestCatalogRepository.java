package com.alshifa.mlms_al_shifa.repository;

import com.alshifa.mlms_al_shifa.model.TestCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TestCatalogRepository
        extends JpaRepository<TestCatalog, Long> {

    List<TestCatalog> findByActiveTrue();

    List<TestCatalog> findByDepartmentIdAndActiveTrue(
            Long departmentId);
}