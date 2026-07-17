// service/impl/CartServiceImpl.java
package com.ecommerce.ecommerce.service.impl;

import com.ecommerce.ecommerce.dto.CartItemRequest;
import com.ecommerce.ecommerce.dto.CartItemResponse;
import com.ecommerce.ecommerce.dto.CartResponse;
import com.ecommerce.ecommerce.entity.Cart;
import com.ecommerce.ecommerce.entity.CartItem;
import com.ecommerce.ecommerce.entity.Product;
import com.ecommerce.ecommerce.entity.User;
import com.ecommerce.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce.repository.CartItemRepository;
import com.ecommerce.ecommerce.repository.CartRepository;
import com.ecommerce.ecommerce.repository.ProductRepository;
import com.ecommerce.ecommerce.repository.UserRepository;
import com.ecommerce.ecommerce.security.SecurityUtil;
import com.ecommerce.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;

    private User getCurrentUser() {
        String email = securityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // "Get or create" — every user gets exactly one Cart, lazily created
    // the first time they interact with it, never requiring an explicit
    // "create my cart" call from the frontend.
    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    @Override
    public CartResponse getMyCart() {
        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);
        return mapToResponse(cart);
    }

    @Override
    public CartResponse addItem(CartItemRequest request) {
        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Already in cart? increment quantity instead of creating a duplicate row.
        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);

        if (item != null) {
            item.setQuantity(item.getQuantity() + request.getQuantity());
        } else {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(request.getQuantity());
        }

        cartItemRepository.save(item);

        // re-fetch the cart so the mapped response reflects the latest item list
        Cart refreshed = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        return mapToResponse(refreshed);
    }

    @Override
    public CartResponse updateItemQuantity(Long itemId, int quantity) {
        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);

        CartItem item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        return mapToResponse(cart);
    }

    @Override
    public CartResponse removeItem(Long itemId) {
        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);

        CartItem item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        cartItemRepository.delete(item);

        Cart refreshed = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        return mapToResponse(refreshed);
    }

    @Override
    public CartResponse clearCart() {
        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);

        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();

        return mapToResponse(cart);
    }

    private CartResponse mapToResponse(Cart cart) {
        List<CartItemResponse> itemResponses = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            BigDecimal subtotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));

            itemResponses.add(new CartItemResponse(
                    item.getId(),
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    item.getQuantity(),
                    subtotal
            ));

            total = total.add(subtotal);
        }

        return new CartResponse(cart.getId(), itemResponses, total);
    }
}