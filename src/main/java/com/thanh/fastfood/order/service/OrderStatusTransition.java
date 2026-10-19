package com.thanh.fastfood.order.service;

import com.thanh.fastfood.order.entity.OrderStatus;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class OrderStatusTransition {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS =
            Map.of(
                    OrderStatus.PENDING,
                    EnumSet.of(
                            OrderStatus.CONFIRMED,
                            OrderStatus.CANCELLED
                    ),

                    OrderStatus.CONFIRMED,
                    EnumSet.of(
                            OrderStatus.PREPARING,
                            OrderStatus.CANCELLED
                    ),

                    OrderStatus.PREPARING,
                    EnumSet.of(
                            OrderStatus.DELIVERING,
                            OrderStatus.CANCELLED
                    ),

                    OrderStatus.DELIVERING,
                    EnumSet.of(
                            OrderStatus.COMPLETED
                    ),

                    OrderStatus.COMPLETED,
                    EnumSet.noneOf(OrderStatus.class),

                    OrderStatus.CANCELLED,
                    EnumSet.noneOf(OrderStatus.class)
            );

    private OrderStatusTransition() {
    }

    public static boolean canTransition(
            OrderStatus currentStatus,
            OrderStatus newStatus
    ) {
        if (currentStatus == null || newStatus == null) {
            return false;
        }

        // Gửi lại trạng thái hiện tại được xem là thành công.
        if (currentStatus == newStatus) {
            return true;
        }

        return ALLOWED_TRANSITIONS
                .getOrDefault(currentStatus, Set.of())
                .contains(newStatus);
    }
}