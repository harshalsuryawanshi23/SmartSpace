package com.smartspace.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddCohostRequest {
    @NotBlank(message = "Display name is required")
    private String displayName;

    private String phone;

    @NotNull(message = "Share amount is required")
    @Positive(message = "Share amount must be positive")
    private BigDecimal shareAmount;
}
