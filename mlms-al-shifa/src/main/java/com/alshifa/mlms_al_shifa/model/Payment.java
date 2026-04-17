package com.alshifa.mlms_al_shifa.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "amount_paid",
            nullable = false,
            precision = 10, scale = 2)
    private BigDecimal amountPaid;

    @Column(name = "payment_method",
            nullable = false, length = 20)
    @Builder.Default
    private String paymentMethod = "CASH";

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "paid_at", nullable = false,
            updatable = false)
    private LocalDateTime paidAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_by")
    private User receivedBy;

    @PrePersist
    protected void onCreate() {
        this.paidAt = LocalDateTime.now();
    }
}