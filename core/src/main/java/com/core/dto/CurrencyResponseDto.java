package com.core.dto;

import java.math.BigDecimal;

public record CurrencyResponseDto(Long id, String code, BigDecimal multiplier) {
}
