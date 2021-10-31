package com.core.dto.product;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

public record ProductDescriptionRequestDto(Long id, @NotBlank @Length(max = 3) String languageCode,
                                           @NotBlank @Length(max = 250) String title,
                                           @NotBlank @Length(max = 2500) String description) {
}
