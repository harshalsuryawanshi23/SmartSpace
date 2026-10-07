package com.smartspace.kyc.controller;

import com.smartspace.auth.jwt.JwtProvider;
import com.smartspace.kyc.dto.ConsentRequest;
import com.smartspace.kyc.dto.KycCompleteRequest;
import com.smartspace.kyc.dto.KycStartResponse;
import com.smartspace.kyc.dto.KycStatusResponse;
import com.smartspace.kyc.service.KycService;
import com.smartspace.user.entity.User;
import com.smartspace.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/kyc")
@RequiredArgsConstructor
public class KycController {

    private final KycService kycService;
    private final UserService userService;

    private Long getUserId(String publicId) {
        return userService.getUserByPublicId(publicId).getId();
    }

    @PostMapping("/consent")
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<Void> recordConsent(@Valid @RequestBody ConsentRequest consentRequest, HttpServletRequest request, @AuthenticationPrincipal String publicId) {
        String ipAddress = request.getRemoteAddr();
        kycService.recordConsent(getUserId(publicId), consentRequest, ipAddress);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/start")
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<KycStartResponse> startKyc(@AuthenticationPrincipal String publicId) {
        return ResponseEntity.ok(kycService.startKyc(getUserId(publicId)));
    }

    @PostMapping("/complete")
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<Void> completeKyc(@Valid @RequestBody KycCompleteRequest completeRequest, @AuthenticationPrincipal String publicId) {
        kycService.completeKyc(getUserId(publicId), completeRequest.sessionId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<KycStatusResponse> getKycStatus(@AuthenticationPrincipal String publicId) {
        return ResponseEntity.ok(kycService.getKycStatus(getUserId(publicId)));
    }

    @PostMapping("/revoke")
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<Void> revokeKyc(@AuthenticationPrincipal String publicId) {
        kycService.revokeKyc(getUserId(publicId));
        return ResponseEntity.ok().build();
    }
}
