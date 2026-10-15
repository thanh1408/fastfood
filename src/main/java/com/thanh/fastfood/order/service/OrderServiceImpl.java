
package com.thanh.fastfood.order.service;

import com.thanh.fastfood.auth.entity.User;
import com.thanh.fastfood.auth.repository.UserRepository;
import com.thanh.fastfood.common.exception.ResourceNotFoundException;
import com.thanh.fastfood.order.dto.CreateOrderRequest;
import com.thanh.fastfood.order.dto.OrderItemResponse;
import com.thanh.fastfood.order.dto.OrderResponse;
import com.thanh.fastfood.order.entity.Order;
import com.thanh.fastfood.order.entity.OrderItem;
import com.thanh.fastfood.order.entity.OrderStatus;
import com.thanh.fastfood.order.repository.OrderItemRepository;
import com.thanh.fastfood.order.repository.OrderRepository;
import com.thanh.fastfood.product.entity.Product;
import com.thanh.fastfood.product.repository.ProductRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            UserRepository userRepository,
            ProductRepository productRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }


    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        // 1. Lấy tài khoản đang đăng nhập
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Bạn cần đăng nhập để đặt hàng"
            );
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy người dùng"
                ));

        // 2. Khởi tạo đơn hàng
        Order order = Order.builder()
                .user(user)
                .deliveryAddress(request.getDeliveryAddress().trim())
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        // 3. Tạo các dòng sản phẩm
        java.util.List<OrderItem> orderItems = new java.util.ArrayList<>();

        for (CreateOrderRequest.OrderItemRequest itemRequest
                : request.getItems()) {

            Product product = productRepository
                    .findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy sản phẩm ID: "
                                    + itemRequest.getProductId()
                    ));

            if (!"AVAILABLE".equalsIgnoreCase(product.getStatus())) {
                throw new IllegalArgumentException(
                        "Sản phẩm hiện không khả dụng: " + product.getName()
                );
            }

            BigDecimal unitPrice = product.getPrice();

            BigDecimal subtotal = unitPrice.multiply(
                    BigDecimal.valueOf(itemRequest.getQuantity())
            );

            totalAmount = totalAmount.add(subtotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(unitPrice)
                    .build();

            orderItems.add(orderItem);
        }

        // 4. Lưu đơn hàng
        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        // 5. Lưu các dòng sản phẩm
        orderItemRepository.saveAll(orderItems);

        // 6. Trả về DTO
        java.util.List<OrderItemResponse> itemResponses = orderItems.stream()
                .map(item -> new OrderItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getUnitPrice().multiply(
                                BigDecimal.valueOf(item.getQuantity())
                        )
                ))
                .toList();

        return new OrderResponse(
                savedOrder.getId(),
                savedOrder.getDeliveryAddress(),
                savedOrder.getStatus(),
                savedOrder.getTotalAmount(),
                itemResponses,
                savedOrder.getCreateAt()
        );
    }

}
