package com.ecommerce.ecommerce.repository;

import com.ecommerce.ecommerce.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Orders, Long>  {
    List<Orders> findByUserId(Long userId);
    Optional<Orders> findByIdAndUserId(Long id, Long userId); // ownership check, same pattern as Address/Cart
}