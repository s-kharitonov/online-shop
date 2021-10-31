package com.core.dto.product;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

public record ProductFeatureRequestDto(Long id, @NotBlank @Length(max = 3) String languageCode,
                                       @NotBlank @Length(max = 150) String name,
                                       @NotBlank @Length(max = 100) String value) {
}
