package com.core.dto.product;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record ProductRequestDto(@NotNull @Min(0) BigDecimal price,
                                @Valid @NotEmpty List<ProductDescriptionRequestDto> descriptions,
                                @Valid @NotEmpty List<ProductFeatureRequestDto> features) {
}
