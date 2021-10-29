package com.core.dto.product;

import org.springframework.lang.NonNull;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;

public record ProductRequestDto(@NonNull @Min(0) BigDecimal price,
                                @Valid @NotEmpty List<ProductDescriptionRequestDto> descriptions,
                                @Valid @NotEmpty List<ProductFeatureRequestDto> features) {
}
