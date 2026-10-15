package com.thanh.fastfood.order.repository;

import com.thanh.fastfood.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}