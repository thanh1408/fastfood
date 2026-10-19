package com.thanh.fastfood.order.dto;

import com.thanh.fastfood.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(

        @NotNull(message = "Trạng thái đơn hàng không được để trống")
        OrderStatus status

) {
}