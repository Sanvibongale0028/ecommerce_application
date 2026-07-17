// dto/CartItemResponse.java
package com.ecommerce.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class CartItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal productPrice;   // current price, at view time
    private int quantity;
    private BigDecimal subtotal;       // productPrice * quantity, computed
}