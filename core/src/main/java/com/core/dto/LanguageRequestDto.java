package com.core.dto;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

public record LanguageRequestDto(@NotBlank @Length(max = 3) String code) {

    private LanguageRequestDto(Builder builder) {
        this(builder.code);
    }

    public static class Builder {
        private String code;

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public LanguageRequestDto build() {
            return new LanguageRequestDto(this);
        }
    }
}
