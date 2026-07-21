package com.ecommerce.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class OrderItemResponse  {

    private Long id;
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal priceAtPurchase; // snapshot — never changes even if product price changes later
    private BigDecimal subtotal;
    
}