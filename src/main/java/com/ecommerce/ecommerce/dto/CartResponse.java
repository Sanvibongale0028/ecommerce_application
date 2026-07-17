// dto/CartResponse.java
package com.ecommerce.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CartResponse {
    private Long id;
    private List<CartItemResponse> items;
    private BigDecimal totalAmount; // sum of all item subtotals
}