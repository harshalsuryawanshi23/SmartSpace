package com.smartspace.user.controller;

import com.smartspace.auth.dto.AuthResponse;
import com.smartspace.user.entity.User;
import com.smartspace.user.service.UserService;
import com.smartspace.user.entity.UserRole;
import com.smartspace.security.auth.SmartSpacePrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final com.smartspace.auth.service.AuthService authService;
    private final com.smartspace.storage.FileStorageService fileStorageService;

    @GetMapping("/me")
    public ResponseEntity<AuthResponse.UserDto> getMe(
            @AuthenticationPrincipal SmartSpacePrincipal principal) {

        User user = userService.getUserByPublicId(
                principal.getPublicId()
        );

        AuthResponse.UserDto dto =
                AuthResponse.UserDto.builder()
                        .publicId(user.getPublicId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .roles(user.getRoles())
                        .build();

        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/me/language")
    public ResponseEntity<Void> updateLanguage(
            @AuthenticationPrincipal SmartSpacePrincipal principal,
            @RequestParam String language) {

        userService.updatePreferredLanguage(
                principal.getPublicId(),
                language
        );

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal SmartSpacePrincipal principal,
            @jakarta.validation.Valid
            @org.springframework.web.bind.annotation.RequestBody
            com.smartspace.auth.dto.ChangePasswordRequest request) {

        authService.changePassword(
                principal.getPublicId(),
                request.getOldPassword(),
                request.getNewPassword()
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping(
            value = "/me/photo",
            consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Void> uploadProfilePhoto(
            @AuthenticationPrincipal SmartSpacePrincipal principal,
            @org.springframework.web.bind.annotation.RequestPart("file")
            org.springframework.web.multipart.MultipartFile file) {

        com.smartspace.storage.FileStorageService.FileMetadata metadata =
                fileStorageService.store(file);

        userService.updateProfilePhoto(
                principal.getPublicId(),
                metadata.getPath()
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/watchmen")
    public ResponseEntity<Void> createWatchman(
            @AuthenticationPrincipal SmartSpacePrincipal principal,
            @jakarta.validation.Valid
            @org.springframework.web.bind.annotation.RequestBody
            com.smartspace.auth.dto.RegisterRequest request) {

        User owner =
                userService.getUserByPublicId(
                        principal.getPublicId()
                );

        if (!owner.getRoles().contains(UserRole.HALL_OWNER)
                && !owner.getRoles().contains(UserRole.ADMIN)) {

            throw new com.smartspace.common.exception.DomainException(
                    com.smartspace.common.exception.ErrorCode.FORBIDDEN,
                    "Not authorized to create watchmen"
            );
        }

        authService.registerWatchman(request);

        return ResponseEntity.ok().build();
    }
}
