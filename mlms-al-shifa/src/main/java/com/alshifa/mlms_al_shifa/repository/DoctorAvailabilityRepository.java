package com.alshifa.mlms_al_shifa.repository;

import com.alshifa.mlms_al_shifa.model.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DoctorAvailabilityRepository
        extends JpaRepository<DoctorAvailability, Long> {

    // Used by AppointmentService.getAvailableSlots()
    List<DoctorAvailability> findByDoctorIdAndSlotDate(
            Long doctorId,
            LocalDate date);

    // Used by AppointmentService.getDoctorCalendar()
    List<DoctorAvailability> findByDoctorIdAndSlotDateBetween(
            Long doctorId,
            LocalDate from,
            LocalDate to);

    // Used by AppointmentService.getDoctorAppointments()
    // Returns all slots that are booked for a doctor
    List<DoctorAvailability> findByDoctorIdAndBookedTrue(
            Long doctorId);

    // Used by AppointmentService.getAllFutureSlots()
    // Returns all unbooked slots from today onward
    @Query("""
        SELECT da FROM DoctorAvailability da
        WHERE da.doctor.id = :doctorId
        AND da.slotDate >= :today
        AND da.booked = false
        ORDER BY da.slotDate ASC, da.startTime ASC
        """)
    List<DoctorAvailability> findFutureSlotsByDoctorId(
            @Param("doctorId") Long doctorId,
            @Param("today") LocalDate today);
}