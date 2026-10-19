
package com.thanh.fastfood.order.controller;

import com.thanh.fastfood.order.dto.CreateOrderRequest;
import com.thanh.fastfood.order.dto.OrderResponse;
import com.thanh.fastfood.order.dto.OrderSummaryResponse;
import com.thanh.fastfood.order.dto.UpdateOrderStatusRequest;
import com.thanh.fastfood.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(
            @Valid @RequestBody CreateOrderRequest request
    ) {
        return orderService.createOrder(request);
    }


    @GetMapping("/{id}")
    public OrderResponse getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }


    @GetMapping("/my")
    public Page<OrderSummaryResponse> getMyOrders(
            @PageableDefault(
                    size = 10,
                    sort = "createAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        return orderService.getMyOrders(pageable);
    }

    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        orderService.updateOrderStatus(id, request);
    }
}
