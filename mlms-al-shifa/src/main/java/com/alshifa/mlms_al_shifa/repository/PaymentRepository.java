package com.alshifa.mlms_al_shifa.repository;

import com.alshifa.mlms_al_shifa.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

    List<Payment> findByInvoiceId(Long invoiceId);
}