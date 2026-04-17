package com.alshifa.mlms_al_shifa.service;

import com.alshifa.mlms_al_shifa.model.Patient;
import com.alshifa.mlms_al_shifa.model.User;
import com.alshifa.mlms_al_shifa.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    /*
     * @Transactional keeps the Hibernate session open
     * so lazy-loaded associations (registeredBy User)
     * do not throw LazyInitializationException in
     * Thymeleaf templates. This was the root cause of
     * blank patient lists.
     */
    @Transactional(readOnly = true)
    public List<Patient> getAll() {
        return patientRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Patient> search(String query) {
        return patientRepository.search(query);
    }

    @Transactional(readOnly = true)
    public Patient getById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Patient not found: " + id));
    }

    public long countAll() {
        return patientRepository.count();
    }

    @Transactional
    public Patient register(
            String fullName,
            LocalDate dob,
            String gender,
            String phone,
            String address,
            String bloodGroup,
            User registrar) {

        // Auto-generate patient code: PAT-YYYYMMDD-SEQ
        long count = patientRepository.count() + 1;
        String code = String.format("PAT-%05d", count);

        Patient patient = Patient.builder()
                .patientCode(code)
                .fullName(fullName)
                .dateOfBirth(dob)
                .gender(gender)
                .phone(phone)
                .address(address)
                .bloodGroup(bloodGroup)
                .registeredBy(registrar)
                .registeredAt(LocalDateTime.now())
                .build();

        return patientRepository.save(patient);
    }
}