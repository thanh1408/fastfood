
package com.thanh.fastfood.order.service;

import com.thanh.fastfood.order.dto.CreateOrderRequest;
import com.thanh.fastfood.order.dto.OrderResponse;
import com.thanh.fastfood.order.dto.OrderSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrderById(Long orderId);

    Page<OrderSummaryResponse> getMyOrders(Pageable pageable);
}
