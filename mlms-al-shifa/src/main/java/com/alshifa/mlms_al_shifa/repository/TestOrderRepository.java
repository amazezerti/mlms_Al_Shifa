package com.alshifa.mlms_al_shifa.repository;

import com.alshifa.mlms_al_shifa.model.TestOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestOrderRepository
        extends JpaRepository<TestOrder, Long> {

    List<TestOrder> findByPatientIdOrderByOrderedAtDesc(
            Long patientId);

    List<TestOrder> findByStatusOrderByOrderedAtDesc(
            String status);

    List<TestOrder> findAllByOrderByOrderedAtDesc();

    /*
     * DRAFT orders for patients assigned to a given
     * department — only doctors in that department see them.
     * JOIN FETCH prevents LazyInitializationException when
     * the template iterates o.patient, o.items, i.test.
     */
    @Query("""
        SELECT DISTINCT o FROM TestOrder o
        JOIN FETCH o.patient p
        LEFT JOIN FETCH o.createdBy
        LEFT JOIN FETCH o.items i
        LEFT JOIN FETCH i.test
        WHERE o.status = 'DRAFT'
        AND p.id IN (
            SELECT pa.patient.id
            FROM PatientAssignment pa
            WHERE pa.department.id = :deptId
            AND pa.status = 'ACTIVE'
        )
        ORDER BY o.orderedAt DESC
        """)
    List<TestOrder> findDraftOrdersByDepartment(
            @Param("deptId") Long deptId);

    /*
     * Fallback: ALL DRAFT orders — used when a doctor
     * has no department profile yet.
     */
    @Query("""
        SELECT DISTINCT o FROM TestOrder o
        JOIN FETCH o.patient
        LEFT JOIN FETCH o.createdBy
        LEFT JOIN FETCH o.items i
        LEFT JOIN FETCH i.test
        WHERE o.status = 'DRAFT'
        ORDER BY o.orderedAt DESC
        """)
    List<TestOrder> findAllDraftOrders();

    /* kept for dashboard count — does not need fetch */
    @Query("""
        SELECT o FROM TestOrder o
        WHERE o.status = 'DRAFT'
        AND o.patient.id IN (
            SELECT pa.patient.id
            FROM PatientAssignment pa
            WHERE pa.doctor.id = :doctorId
            AND pa.status = 'ACTIVE'
        )
        ORDER BY o.orderedAt DESC
        """)
    List<TestOrder> findDraftOrdersForDoctor(
            @Param("doctorId") Long doctorId);

    @Query("""
        SELECT COUNT(o) FROM TestOrder o
        WHERE o.status = 'DRAFT'
        AND o.patient.id IN (
            SELECT pa.patient.id
            FROM PatientAssignment pa
            WHERE pa.doctor.id = :doctorId
            AND pa.status = 'ACTIVE'
        )
        """)
    long countByStatusAndAssignedDoctor(
            @Param("status") String status,
            @Param("doctorId") Long doctorId);

    /*
     * FIX: Lab technician sees ALL orders that need
     * lab work — CONFIRMED and IN_PROGRESS (not just CONFIRMED).
     * Previously only CONFIRMED was returned so orders
     * disappeared after sample collection.
     */
    @Query("""
        SELECT DISTINCT o FROM TestOrder o
        JOIN FETCH o.patient
        LEFT JOIN FETCH o.items i
        LEFT JOIN FETCH i.test
        WHERE o.status IN ('CONFIRMED','IN_PROGRESS')
        ORDER BY o.orderedAt DESC
        """)
    List<TestOrder> findConfirmedOrders();

    /*
     * NEW: All orders regardless of status,
     * for history/report views.
     */
    @Query("""
        SELECT o FROM TestOrder o
        ORDER BY o.orderedAt DESC
        """)
    List<TestOrder> findAllOrdersHistory();

    @Query("""
        SELECT o FROM TestOrder o
        WHERE o.patient.id = :patientId
        ORDER BY o.orderedAt DESC
        """)
    List<TestOrder> findByPatient(
            @Param("patientId") Long patientId);

    /*
     * NEW: Orders for a specific technician
     * based on samples they collected.
     */
    @Query("""
        SELECT DISTINCT o FROM TestOrder o
        JOIN o.items i
        LEFT JOIN TestResult tr ON tr.orderItem = i
        WHERE tr.enteredBy.id = :techId
        ORDER BY o.orderedAt DESC
        """)
    List<TestOrder> findOrdersByTechnician(
            @Param("techId") Long techId);

    /*
     * All non-cancelled orders — shown in the
     * receptionist test orders list.
     */
    @Query("""
        SELECT o FROM TestOrder o
        WHERE o.status <> 'CANCELLED'
        ORDER BY o.orderedAt DESC
        """)
    List<TestOrder> findAllActiveOrders();

    /*
     * Orders eligible for invoicing:
     * confirmed or further along.
     */
    @Query("""
        SELECT o FROM TestOrder o
        WHERE o.status IN (
            'CONFIRMED','IN_PROGRESS','COMPLETED')
        ORDER BY o.orderedAt DESC
        """)
    List<TestOrder> findInvoiceableOrders();
}