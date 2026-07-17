// service/CartService.java
package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.dto.CartItemRequest;
import com.ecommerce.ecommerce.dto.CartResponse;

public interface CartService {
    CartResponse getMyCart();
    CartResponse addItem(CartItemRequest request);
    CartResponse updateItemQuantity(Long itemId, int quantity);
    CartResponse removeItem(Long itemId);
    CartResponse clearCart();
}