package com.smartspace.auth.dto;

import com.smartspace.user.entity.UserRole;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private UserDto user;
    private boolean requiresPasswordChange;

    @Data
    @Builder
    public static class UserDto {
        private String publicId;
        private String fullName;
        private String email;
        private String phone;
        private Set<UserRole> roles;
    }
}
