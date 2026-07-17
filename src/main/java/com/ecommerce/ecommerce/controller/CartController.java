// controller/CartController.java
package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.dto.CartItemRequest;
import com.ecommerce.ecommerce.dto.CartResponse;
import com.ecommerce.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getMyCart() {
        return ResponseEntity.ok(cartService.getMyCart());
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(request));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @PathVariable Long itemId, @RequestParam int quantity) {
        return ResponseEntity.ok(cartService.updateItemQuantity(itemId, quantity));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(itemId));
    }

    @DeleteMapping
    public ResponseEntity<CartResponse> clearCart() {
        return ResponseEntity.ok(cartService.clearCart());
    }
}
