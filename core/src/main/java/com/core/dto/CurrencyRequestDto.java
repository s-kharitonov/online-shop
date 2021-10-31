package com.core.dto;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CurrencyRequestDto(@NotBlank @Length(min = 3, max = 3) String code,
                                 @NotNull @Min(value = 0) BigDecimal multiplier) {
}
