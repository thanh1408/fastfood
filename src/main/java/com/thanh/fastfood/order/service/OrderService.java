
package com.thanh.fastfood.order.service;

import com.thanh.fastfood.order.dto.CreateOrderRequest;
import com.thanh.fastfood.order.dto.OrderResponse;
import com.thanh.fastfood.order.dto.OrderSummaryResponse;
import com.thanh.fastfood.order.dto.UpdateOrderStatusRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrderById(Long orderId);

    Page<OrderSummaryResponse> getMyOrders(Pageable pageable);

    void updateOrderStatus(Long orderId, UpdateOrderStatusRequest request);
}
