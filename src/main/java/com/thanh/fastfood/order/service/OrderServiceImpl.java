
package com.thanh.fastfood.order.service;

import com.thanh.fastfood.auth.entity.User;
import com.thanh.fastfood.auth.repository.UserRepository;
import com.thanh.fastfood.common.exception.InvalidOrderStatusException;
import com.thanh.fastfood.common.exception.ResourceNotFoundException;
import com.thanh.fastfood.order.dto.*;
import com.thanh.fastfood.order.entity.Order;
import com.thanh.fastfood.order.entity.OrderItem;
import com.thanh.fastfood.order.entity.OrderStatus;
import com.thanh.fastfood.order.repository.OrderItemRepository;
import com.thanh.fastfood.order.repository.OrderRepository;
import com.thanh.fastfood.product.entity.Product;
import com.thanh.fastfood.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

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


    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {

        // 1. Tìm đơn hàng
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy đơn hàng ID: " + orderId
                ));

        // 2. Lấy người dùng đang đăng nhập
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {
            throw new AccessDeniedException("Bạn chưa đăng nhập");
        }

        String email = authentication.getName();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));

        boolean isOwner = order.getUser() != null
                && Objects.equals(
                order.getUser().getEmail(),
                email
        );

        // 3. Chỉ ADMIN hoặc chủ đơn hàng được xem
        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException(
                    "Bạn không có quyền xem đơn hàng này"
            );
        }

        // 4. Lấy các dòng sản phẩm
        List<OrderItemResponse> itemResponses =
                orderItemRepository.findByOrderId(orderId)
                        .stream()
                        .map(this::toOrderItemResponse)
                        .toList();

        // 5. Chuyển Order thành response DTO
        return new OrderResponse(
                order.getId(),
                order.getDeliveryAddress(),
                order.getStatus(),
                order.getTotalAmount(),
                itemResponses,
                order.getCreateAt()
        );
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getUnitPrice().multiply(
                        java.math.BigDecimal.valueOf(item.getQuantity())
                )
        );
    }


    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getMyOrders(Pageable pageable) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Bạn cần đăng nhập để xem lịch sử đơn hàng"
            );
        }

        return orderRepository
                .findByUserEmailOrderByCreateAtDesc(
                        authentication.getName(),
                        pageable
                )
                .map(order -> new OrderSummaryResponse(
                        order.getId(),
                        order.getDeliveryAddress(),
                        order.getStatus(),
                        order.getTotalAmount(),
                        order.getCreateAt()
                ));
    }


    @Override
    @Transactional
    public void updateOrderStatus(
            Long orderId,
            UpdateOrderStatusRequest request
    ) {
        // 1. Tìm đơn hàng
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy đơn hàng ID: " + orderId
                ));

        // 2. Lấy trạng thái hiện tại và trạng thái được yêu cầu
        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = request.status();

        // 3. Nếu trạng thái không thay đổi, kết thúc thành công
        if (currentStatus == newStatus) {
            return;
        }

        // 4. Kiểm tra quy tắc chuyển trạng thái
        if (!OrderStatusTransition.canTransition(currentStatus, newStatus)) {
            throw new InvalidOrderStatusException(
                    "Không thể chuyển trạng thái từ "
                            + currentStatus
                            + " sang "
                            + newStatus
            );
        }

        // 5. Cập nhật trạng thái
        order.setStatus(newStatus);

    }

}
