package com.alshifa.mlms_al_shifa.repository;

import com.alshifa.mlms_al_shifa.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderItemRepository
        extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    @Query("""
        SELECT oi FROM OrderItem oi
        WHERE oi.order.status IN ('CONFIRMED', 'IN_PROGRESS')
        AND oi.itemStatus = 'PENDING'
        ORDER BY oi.order.orderedAt DESC
        """)
    List<OrderItem> findPendingConfirmedItems();
}