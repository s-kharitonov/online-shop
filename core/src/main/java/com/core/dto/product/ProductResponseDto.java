package com.core.dto.product;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponseDto(Long id, BigDecimal price, List<ProductDescriptionResponseDto> descriptions,
                                 List<ProductFeatureResponseDto> features) {
}
