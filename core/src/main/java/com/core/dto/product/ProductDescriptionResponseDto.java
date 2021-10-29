package com.core.dto.product;

import com.core.dto.LanguageResponseDto;

public record ProductDescriptionResponseDto(Long id, LanguageResponseDto language, String title, String description) {
}
