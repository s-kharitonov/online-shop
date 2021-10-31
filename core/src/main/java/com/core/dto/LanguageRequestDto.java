package com.core.dto;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

public record LanguageRequestDto(@NotBlank @Length(max = 3) String code) {
}
