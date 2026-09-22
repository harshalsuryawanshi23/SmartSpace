package com.smartspace.dispute.dto;

import com.smartspace.dispute.entity.DisputeCategory;
import com.smartspace.dispute.entity.DisputeSide;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DisputeRaiseRequest {
    @NotNull
    private Long bookingId;

    @NotNull
    private DisputeSide againstSide;

    @NotNull
    private DisputeCategory category;

    @NotBlank
    private String description;

    private BigDecimal claimedAmount;
}
