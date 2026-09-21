package com.smartspace.kyc.controller;

import com.smartspace.auth.jwt.JwtProvider;
import com.smartspace.kyc.dto.ConsentRequest;
import com.smartspace.kyc.dto.KycCompleteRequest;
import com.smartspace.kyc.dto.KycStartResponse;
import com.smartspace.kyc.dto.KycStatusResponse;
import com.smartspace.kyc.service.KycService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/kyc")
@RequiredArgsConstructor
public class KycController {

    private final KycService kycService;
    private final JwtProvider jwtProvider;

    private Long getUserId(HttpServletRequest request) {
        String token = jwtProvider.extractToken(request);
        return Long.parseLong(jwtProvider.getSubject(token));
    }

    @PostMapping("/consent")
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<Void> recordConsent(@Valid @RequestBody ConsentRequest consentRequest, HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        kycService.recordConsent(getUserId(request), consentRequest, ipAddress);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/start")
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<KycStartResponse> startKyc(HttpServletRequest request) {
        return ResponseEntity.ok(kycService.startKyc(getUserId(request)));
    }

    @PostMapping("/complete")
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<Void> completeKyc(@Valid @RequestBody KycCompleteRequest completeRequest, HttpServletRequest request) {
        kycService.completeKyc(getUserId(request), completeRequest.sessionId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<KycStatusResponse> getKycStatus(HttpServletRequest request) {
        return ResponseEntity.ok(kycService.getKycStatus(getUserId(request)));
    }

    @PostMapping("/revoke")
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<Void> revokeKyc(HttpServletRequest request) {
        kycService.revokeKyc(getUserId(request));
        return ResponseEntity.ok().build();
    }
}
