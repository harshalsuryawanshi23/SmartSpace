package com.smartspace.dispute.dto;

import com.smartspace.dispute.entity.Dispute;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DisputeResolveRequest {
    @NotNull
    private Dispute.Status status;

    @NotBlank
    private String resolutionNote;
    
    // e.g. how much to refund, trust penalty applied, etc.
}
