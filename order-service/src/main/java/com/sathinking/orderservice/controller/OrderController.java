package com.sathinking.orderservice.controller;

import com.sathinking.orderservice.client.ProductClient;
import com.sathinking.orderservice.config.ShardResolver;
import com.sathinking.orderservice.config.ShardRoutingDataSource;
import com.sathinking.orderservice.dto.*;
import com.sathinking.orderservice.entity.Order;
import com.sathinking.orderservice.entity.OrderStatus;
import com.sathinking.orderservice.repository.OrderRepository;
import com.sathinking.orderservice.service.OrderCacheService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final OrderCacheService orderCacheService; // add alongside existing fields
    private final ShardResolver shardResolver; // add alongside existing fields




    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id, @RequestParam Long customerId) {
        String shard = shardResolver.resolveShard(customerId);
        ShardRoutingDataSource.setShard(shard);
        try {
            OrderResponse response = orderCacheService.getById(id, customerId);
            return response != null ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
        } finally {
            ShardRoutingDataSource.clearShard();
        }
    }

    @GetMapping
    public List<OrderResponse> getAll() {
        return orderRepository.findAll().stream()
                .map(order -> {
                    String productName;
                    try {
                        productName = productClient.getProduct(order.getProductId()).getName();
                    } catch (Exception e) {
                        productName = "Unknown";
                    }
                    return toResponse(order, productName);
                })
                .collect(Collectors.toList());
    }

    private OrderResponse toResponse(Order order, String productName) {
        return OrderResponse.builder()
                .id(order.getId())
                .productId(order.getProductId())
                .productName(productName)
                .customerId(order.getCustomerId())   // add this line
                .quantity(order.getQuantity())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam Long customerId, @RequestParam OrderStatus status) {
        String shard = shardResolver.resolveShard(customerId);
        ShardRoutingDataSource.setShard(shard);
        try {
            return orderRepository.findById(id)
                    .map(order -> {
                        order.setStatus(status);
                        Order updated = orderRepository.save(order);
                        String productName = productClient.getProduct(updated.getProductId()).getName();
                        OrderResponse response = toResponse(updated, productName);
                        orderCacheService.put(response);
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } finally {
            ShardRoutingDataSource.clearShard();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam Long customerId) {
        String shard = shardResolver.resolveShard(customerId);
        ShardRoutingDataSource.setShard(shard);
        try {
            if (!orderRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            orderRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } finally {
            ShardRoutingDataSource.clearShard();
        }
    }

    public ResponseEntity<?> productServiceFallback(OrderRequest request, Throwable t) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Product Service is currently unavailable. Please try again shortly.");
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody OrderRequest request) {
        String shard = shardResolver.resolveShard(request.getCustomerId());
        ShardRoutingDataSource.setShard(shard);
        try {
            ProductResponse product;
            try {
                product = productClient.getProduct(request.getProductId());
            } catch (HttpClientErrorException.NotFound e) {
                return ResponseEntity.badRequest().body("Product not found: " + request.getProductId());
            }

            if (product.getStockQuantity() < request.getQuantity()) {
                return ResponseEntity.badRequest().body("Insufficient stock for product: " + product.getName());
            }

            BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

            Order order = Order.builder()
                    .customerId(request.getCustomerId())
                    .productId(request.getProductId())
                    .quantity(request.getQuantity())
                    .totalPrice(totalPrice)
                    .status(OrderStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();

            Order saved = orderRepository.save(order);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved, product.getName()));
        } finally {
            ShardRoutingDataSource.clearShard();
        }
    }

    private final List<String> ALL_SHARDS = List.of("shard-0", "shard-1", "shard-2");

    @GetMapping("/all")
    public List<OrderResponse> getAllAcrossShards() {
        List<OrderResponse> combined = new ArrayList<>();

        for (String shard : ALL_SHARDS) {
            ShardRoutingDataSource.setShard(shard);
            try {
                List<Order> shardOrders = orderRepository.findAll();
                for (Order order : shardOrders) {
                    String productName;
                    try {
                        productName = productClient.getProduct(order.getProductId()).getName();
                    } catch (Exception e) {
                        productName = "Unknown";
                    }
                    combined.add(toResponse(order, productName));
                }
            } finally {
                ShardRoutingDataSource.clearShard();
            }
        }

        return combined;
    }
}