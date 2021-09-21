package com.core.dto;

public record LanguageResponseDto(Long id, String code) {

    private LanguageResponseDto(Builder builder) {
        this(builder.id, builder.code);
    }

    public static class Builder {
        private Long id;
        private String code;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public LanguageResponseDto build() {
            return new LanguageResponseDto(this);
        }
    }
}
