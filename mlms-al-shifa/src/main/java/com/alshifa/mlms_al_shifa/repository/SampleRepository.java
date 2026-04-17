package com.alshifa.mlms_al_shifa.repository;

import com.alshifa.mlms_al_shifa.model.Sample;
import com.alshifa.mlms_al_shifa.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SampleRepository
        extends JpaRepository<Sample, Long> {

    Optional<Sample> findByBarcode(String barcode);
    boolean existsByBarcode(String barcode);

    @Query("""
        SELECT s FROM Sample s
        WHERE s.status IN ('COLLECTED','RECEIVED')
        ORDER BY s.collectedAt DESC
        """)
    List<Sample> findPendingSamples();

    List<Sample> findByCollectedBy(User tech);

    List<Sample> findByOrderId(Long orderId);
}