package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.dto.OrderResponse;
import com.ecommerce.ecommerce.dto.OrderStatusUpdateRequest;
import com.ecommerce.ecommerce.dto.PlaceOrderRequest;

import java.util.List;

public interface OrderService {

    OrderResponse placeOrder(PlaceOrderRequest request);
    List<OrderResponse> getMyOrders();
    OrderResponse getOrderById(Long orderId);
    OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request); // admin only
    
}