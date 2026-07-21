// service/impl/OrderServiceImpl.java
package com.ecommerce.ecommerce.service.impl;

import com.ecommerce.ecommerce.dto.OrderItemResponse;
import com.ecommerce.ecommerce.dto.OrderResponse;
import com.ecommerce.ecommerce.dto.OrderStatusUpdateRequest;
import com.ecommerce.ecommerce.dto.PlaceOrderRequest;
import com.ecommerce.ecommerce.entity.*;
import com.ecommerce.ecommerce.exception.BadRequestException;
import com.ecommerce.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce.repository.*;
import com.ecommerce.ecommerce.security.SecurityUtil;
import com.ecommerce.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;

    private User getCurrentUser() {
        String email = securityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        User user = getCurrentUser();

        // Address must belong to this user — same ownership-check pattern as Address/Cart
        Address address = addressRepository.findByIdAndUserId(request.getAddressId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot place an order with an empty cart");
        }

        // --- Stock validation pass FIRST, before touching anything ---
        // This is what makes the operation "all or nothing": if any single item
        // fails here, we throw before a single row has been written or a single
        // stock count has been decremented. Nothing needs manual rollback.
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getStock() < cartItem.getQuantity()) {
                throw new BadRequestException(
                        "Insufficient stock for product: " + product.getName()
                                + " (requested " + cartItem.getQuantity()
                                + ", available " + product.getStock() + ")"
                );
            }
        }

        // --- All items are available — now actually build the order ---
        Orders order = new Orders();
        order.setUser(user);
        order.setAddress(address);
        order.setStatus(OrderStatus.PLACED);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            // Snapshot price NOW — if product price changes later, this historical
            // order still reflects what the customer actually paid.
            BigDecimal priceAtPurchase = product.getPrice();
            BigDecimal subtotal = priceAtPurchase.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtPurchase(priceAtPurchase);
            orderItems.add(orderItem);

            // Decrement stock
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);

        Orders savedOrder = orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);

        // Clear the cart now that items have become an order
        cartItemRepositoryDeleteAll(cart);

        return mapToResponse(savedOrder);
    }

    // Small private helper just to keep placeOrder() readable —
    // deletes all cart items and clears the in-memory collection.
    private void cartItemRepositoryDeleteAll(Cart cart) {
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    @Override
    public List<OrderResponse> getMyOrders() {
        User user = getCurrentUser();
        return orderRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public OrderResponse getOrderById(Long orderId) {
        User user = getCurrentUser();
        Orders order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return mapToResponse(order);
    }

    @Override
    public OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid order status: " + request.getStatus());
        }

        validateStatusTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);

        Orders updated = orderRepository.save(order);
        return mapToResponse(updated);
    }

    // Simple state machine — only allows forward transitions along the
    // natural order lifecycle, and blocks illegal jumps (e.g. PLACED -> DELIVERED
    // directly, skipping CONFIRMED/SHIPPED). CANCELLED is allowed from any
    // non-terminal state.
    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        if (current == OrderStatus.DELIVERED || current == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot change status of an order that is already " + current);
        }

        if (next == OrderStatus.CANCELLED) {
            return; // cancellation allowed from PLACED/CONFIRMED/SHIPPED
        }

        boolean validForwardMove =
                (current == OrderStatus.PLACED && next == OrderStatus.CONFIRMED) ||
                (current == OrderStatus.CONFIRMED && next == OrderStatus.SHIPPED) ||
                (current == OrderStatus.SHIPPED && next == OrderStatus.DELIVERED);

        if (!validForwardMove) {
            throw new BadRequestException(
                    "Invalid status transition: " + current + " -> " + next
            );
        }
    }

    private OrderResponse mapToResponse(Orders order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getPriceAtPurchase(),
                        item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity()))
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getAddress().getId(),
                itemResponses,
                order.getCreatedAt()
        );
    }
}