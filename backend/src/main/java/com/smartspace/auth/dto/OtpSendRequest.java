package com.smartspace.auth.dto;

import com.smartspace.auth.entity.OtpPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OtpSendRequest {
    @NotBlank
    private String target; // email or phone

    @NotNull
    private OtpPurpose purpose;

    private Long userId; // Optional, required for some purposes like password reset
}
