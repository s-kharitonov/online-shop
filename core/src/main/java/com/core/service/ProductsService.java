package com.core.service;

import com.core.dto.product.ProductRequestDto;
import com.core.dto.product.ProductResponseDto;

import java.util.Optional;

public interface ProductsService {
    ProductResponseDto create(ProductRequestDto requestDto);

    Optional<ProductResponseDto> getById(long id);

    ProductResponseDto update(long id, ProductRequestDto requestDto);

    void deleteById(long id);
}
