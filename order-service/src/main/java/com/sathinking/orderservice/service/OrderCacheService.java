// OrderCacheService.java
package com.sathinking.orderservice.service;

import com.sathinking.orderservice.client.ProductClient;
import com.sathinking.orderservice.dto.OrderResponse;
import com.sathinking.orderservice.dto.ProductResponse;
import com.sathinking.orderservice.entity.Order;
import com.sathinking.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderCacheService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    @Cacheable(value = "orders", key = "#customerId + '-' + #id")
    public OrderResponse getById(Long id, Long customerId) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) return null;
        ProductResponse product = productClient.getProduct(order.getProductId());
        return toResponse(order, product.getName());
    }

    @CachePut(value = "orders", key = "#response.id")
    public OrderResponse put(OrderResponse response) {
        return response;
    }

    private OrderResponse toResponse(Order order, String productName) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())   // add this line
                .productId(order.getProductId())
                .productName(productName)
                .quantity(order.getQuantity())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .build();
    }
}