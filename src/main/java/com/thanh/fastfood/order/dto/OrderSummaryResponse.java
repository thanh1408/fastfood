
package com.thanh.fastfood.order.dto;

import com.thanh.fastfood.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryResponse(
        Long id,
        String deliveryAddress,
        OrderStatus status,
        BigDecimal totalAmount,
        LocalDateTime createdAt
) {
}
