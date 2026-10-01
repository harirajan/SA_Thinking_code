package com.sathinking.productservice.service;

import com.sathinking.productservice.dto.ProductResponse;
import com.sathinking.productservice.entity.Product;
import com.sathinking.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;



    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public ProductResponse getById(Long id) {
        return productRepository.findById(id).map(this::toResponse).orElse(null);
    }

    @CacheEvict(value = "products", key = "#id")
    public void evict(Long id) {
        // body intentionally empty — annotation does the work
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .build();
    }
}