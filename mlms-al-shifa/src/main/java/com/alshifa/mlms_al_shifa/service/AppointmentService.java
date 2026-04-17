package com.alshifa.mlms_al_shifa.service;

import com.alshifa.mlms_al_shifa.model.DoctorAvailability;
import com.alshifa.mlms_al_shifa.model.Patient;
import com.alshifa.mlms_al_shifa.repository
        .DoctorAvailabilityRepository;
import com.alshifa.mlms_al_shifa.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final DoctorAvailabilityRepository
            availabilityRepository;
    private final PatientRepository patientRepository;

    @Transactional(readOnly = true)
    public List<DoctorAvailability> getAvailableSlots(
            Long doctorId, LocalDate date) {
        return availabilityRepository
                .findByDoctorIdAndSlotDate(doctorId, date);
    }

    @Transactional(readOnly = true)
    public List<DoctorAvailability> getDoctorCalendar(
            Long doctorId,
            LocalDate from,
            LocalDate to) {
        return availabilityRepository
                .findByDoctorIdAndSlotDateBetween(
                        doctorId, from, to);
    }

    @Transactional
    public void bookSlot(
            Long slotId,
            Long patientId,
            String reason) {

        DoctorAvailability slot =
                availabilityRepository.findById(slotId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Slot not found"));

        if (slot.isBooked()) {
            throw new RuntimeException(
                    "This slot is already booked.");
        }

        Patient patient = patientId != null
                ? patientRepository.findById(patientId)
                .orElse(null)
                : null;

        slot.setBooked(true);
        slot.setBookedPatient(patient);
        slot.setAppointmentReason(reason);
        availabilityRepository.save(slot);
    }

    @Transactional
    public void addAvailabilitySlot(
            Long doctorId,
            LocalDate slotDate,
            LocalTime startTime,
            LocalTime endTime) {

        com.alshifa.mlms_al_shifa.model.User doctor =
                new com.alshifa.mlms_al_shifa.model.User();
        doctor.setId(doctorId);

        DoctorAvailability slot =
                DoctorAvailability.builder()
                        .doctor(doctor)
                        .slotDate(slotDate)
                        .startTime(startTime)
                        .endTime(endTime)
                        .booked(false)
                        .build();

        availabilityRepository.save(slot);
    }

    @Transactional(readOnly = true)
    public List<DoctorAvailability> getDoctorAppointments(
            Long doctorId) {
        return availabilityRepository
                .findByDoctorIdAndBookedTrue(doctorId);
    }

    /*
     * Returns all unbooked future slots for a doctor —
     * used by the receptionist appointment booking page.
     */
    @Transactional(readOnly = true)
    public List<DoctorAvailability> getAllFutureSlots(
            Long doctorId) {
        return availabilityRepository
                .findFutureSlotsByDoctorId(
                        doctorId, LocalDate.now());
    }
}