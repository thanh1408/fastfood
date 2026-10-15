package com.thanh.fastfood.order.repository;

import com.thanh.fastfood.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findById(Long id);


    Page<Order> findByUserEmailOrderByCreateAtDesc(
            String email,
            Pageable pageable
    );

}