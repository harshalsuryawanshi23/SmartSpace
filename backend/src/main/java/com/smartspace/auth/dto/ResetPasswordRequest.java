package com.smartspace.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank
    private String target; // email or phone
    
    @NotBlank
    private String newPassword;
}
