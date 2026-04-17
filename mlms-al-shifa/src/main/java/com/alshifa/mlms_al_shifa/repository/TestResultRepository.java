package com.alshifa.mlms_al_shifa.repository;

import com.alshifa.mlms_al_shifa.model.TestResult;
import com.alshifa.mlms_al_shifa.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestResultRepository
        extends JpaRepository<TestResult, Long> {

    @Query("""
        SELECT tr FROM TestResult tr
        JOIN tr.orderItem oi
        JOIN oi.order o
        LEFT JOIN o.confirmedBy cb
        WHERE tr.status = :status
        AND cb = :doctor
        ORDER BY tr.enteredAt DESC
        """)
    List<TestResult> findByStatusAndOrderItemOrderConfirmedBy(
            @Param("status") String status,
            @Param("doctor") User doctor);

    @Query("""
        SELECT tr FROM TestResult tr
        JOIN tr.orderItem oi
        JOIN oi.order o
        WHERE o.patient.id = :patientId
        AND tr.status = 'VERIFIED'
        ORDER BY tr.verifiedAt DESC
        """)
    List<TestResult> findVerifiedByPatient(
            @Param("patientId") Long patientId);

    @Query("""
        SELECT tr FROM TestResult tr
        JOIN tr.orderItem oi
        JOIN oi.order o
        WHERE o.id = :orderId
        AND tr.status = 'VERIFIED'
        ORDER BY tr.verifiedAt DESC
        """)
    List<TestResult> findVerifiedByOrderId(
            @Param("orderId") Long orderId);

    @Query("""
        SELECT tr FROM TestResult tr
        JOIN tr.orderItem oi
        JOIN oi.order o
        WHERE o.id = :orderId
        ORDER BY tr.enteredAt DESC
        """)
    List<TestResult> findAllByOrderId(
            @Param("orderId") Long orderId);

    @Query("""
        SELECT tr FROM TestResult tr
        LEFT JOIN tr.enteredBy eb
        WHERE eb = :tech
        ORDER BY tr.enteredAt DESC
        """)
    List<TestResult> findAllByTechnician(
            @Param("tech") User tech);

    @Query("""
        SELECT tr FROM TestResult tr
        LEFT JOIN tr.enteredBy eb
        WHERE eb = :tech
        AND tr.status = :status
        ORDER BY tr.enteredAt DESC
        """)
    List<TestResult> findByStatusAndTechnician(
            @Param("status") String status,
            @Param("tech") User tech);

    @Query("""
        SELECT tr FROM TestResult tr
        WHERE tr.status = 'VERIFIED'
        ORDER BY tr.verifiedAt DESC
        """)
    List<TestResult> findAllVerifiedResults();

    /*
     * NEW: used by checkAndCompleteOrder to verify
     * whether all items are done.
     */
    boolean existsByOrderItemIdAndStatus(
            Long orderItemId, String status);
}