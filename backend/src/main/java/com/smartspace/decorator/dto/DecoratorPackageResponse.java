package com.smartspace.decorator.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class DecoratorPackageResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private boolean active;
}
