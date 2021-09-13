package com.core.dto;

import java.math.BigDecimal;

public record CurrencyResponseDto(Long id, String code, BigDecimal multiplier) {

    private CurrencyResponseDto(Builder builder) {
        this(builder.id, builder.code, builder.multiplier);
    }

    public static class Builder {
        private Long id;
        private String code;
        private BigDecimal multiplier;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder multiplier(BigDecimal multiplier) {
            this.multiplier = multiplier;
            return this;
        }

        public CurrencyResponseDto build() {
            return new CurrencyResponseDto(this);
        }
    }
}
