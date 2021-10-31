package com.core.dto.product;

import com.core.dto.LanguageResponseDto;

public record ProductFeatureResponseDto(Long id, LanguageResponseDto language, String name, String value) {
}
