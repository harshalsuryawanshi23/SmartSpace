package com.smartspace.auth.dto;

import com.smartspace.user.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    @Size(min = 2, max = 120)
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 10, max = 16)
    private String phone;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    @NotNull
    private UserRole role; // Mostly Resident or HallOwner for self-registration
}
