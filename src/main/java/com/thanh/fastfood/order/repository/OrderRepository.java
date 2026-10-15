package com.thanh.fastfood.order.repository;

import com.thanh.fastfood.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}