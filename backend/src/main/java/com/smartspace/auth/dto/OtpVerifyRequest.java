package com.smartspace.auth.dto;

import com.smartspace.auth.entity.OtpPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OtpVerifyRequest {
    @NotBlank
    private String publicId; // The ID of the OTP challenge returned when sent

    @NotBlank
    private String target; // email or phone

    @NotNull
    private OtpPurpose purpose;

    @NotBlank
    private String otp;
}
