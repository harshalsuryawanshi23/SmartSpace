package com.smartspace.user.controller;

import com.smartspace.auth.dto.AuthResponse;
import com.smartspace.user.entity.User;
import com.smartspace.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.smartspace.user.entity.UserRole;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final com.smartspace.auth.service.AuthService authService;
    private final com.smartspace.storage.FileStorageService fileStorageService;

    @GetMapping("/me")
    public ResponseEntity<AuthResponse.UserDto> getMe(@AuthenticationPrincipal(expression = "publicId") String publicId) {
        User user = userService.getUserByPublicId(publicId);
        AuthResponse.UserDto dto = AuthResponse.UserDto.builder()
                .publicId(user.getPublicId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roles(user.getRoles())
                .build();
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/me/language")
    public ResponseEntity<Void> updateLanguage(@AuthenticationPrincipal(expression = "publicId") String publicId, @RequestParam String language) {
        userService.updatePreferredLanguage(publicId, language);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal(expression = "publicId") String publicId, @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody com.smartspace.auth.dto.ChangePasswordRequest request) {
        // Need to wire authService here, but since it's a cross-concern, we can either inject authService in UserController or put changePassword in UserService
        // We'll wire AuthService since it holds password encoding and refresh token revocation
        authService.changePassword(publicId, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/me/photo", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadProfilePhoto(
            @AuthenticationPrincipal(expression = "publicId") String publicId,
            @org.springframework.web.bind.annotation.RequestPart("file") org.springframework.web.multipart.MultipartFile file) {
        
        com.smartspace.storage.FileStorageService.FileMetadata metadata = fileStorageService.store(file);
        userService.updateProfilePhoto(publicId, metadata.getPath());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/watchmen")
    public ResponseEntity<Void> createWatchman(@AuthenticationPrincipal(expression = "publicId") String publicId, @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody com.smartspace.auth.dto.RegisterRequest request) {
        // Enforce owner/admin permission to create watchmen
        User owner = userService.getUserByPublicId(publicId);
        if (!owner.getRoles().contains(UserRole.HALL_OWNER) && !owner.getRoles().contains(UserRole.ADMIN)) {
            throw new com.smartspace.common.exception.DomainException(com.smartspace.common.exception.ErrorCode.FORBIDDEN, "Not authorized to create watchmen");
        }
        
        // This leverages existing authService logic but forces role to WATCHMAN
        // Wait, authService.register sets role to RESIDENT. Let's add a registerWatchman method in authService or just set it manually if we do it in userService.
        // It's cleaner to add it to authService.
        authService.registerWatchman(request);
        return ResponseEntity.ok().build();
    }
}
