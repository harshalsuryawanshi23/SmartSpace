package com.smartspace.auth.controller;

import com.smartspace.auth.dto.AuthResponse;
import com.smartspace.auth.dto.LoginRequest;
import com.smartspace.auth.dto.OtpSendRequest;
import com.smartspace.auth.dto.OtpVerifyRequest;
import com.smartspace.auth.dto.RegisterRequest;
import com.smartspace.auth.service.AuthService;
import com.smartspace.auth.service.OtpService;
import com.smartspace.common.exception.ApiError;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        
        String userAgent = httpRequest.getHeader(HttpHeaders.USER_AGENT);
        String ip = httpRequest.getRemoteAddr();

        AuthService.AuthResult result = authService.login(request, userAgent, ip);

        setRefreshTokenCookie(httpResponse, result.rawRefreshToken);
        return ResponseEntity.ok(result.response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userAgent = httpRequest.getHeader(HttpHeaders.USER_AGENT);
        String ip = httpRequest.getRemoteAddr();

        AuthService.AuthResult result = authService.refresh(refreshToken, userAgent, ip);

        setRefreshTokenCookie(httpResponse, result.rawRefreshToken);
        return ResponseEntity.ok(result.response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletResponse httpResponse) {
        
        authService.logout(refreshToken);
        clearRefreshTokenCookie(httpResponse);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/otp/send")
    public ResponseEntity<Map<String, String>> sendOtp(@Valid @RequestBody OtpSendRequest request) {
        // In dev, we might return the OTP for easy testing if outbox is not yet fully configured.
        String rawOtp = otpService.generateAndSaveOtp(request.getTarget(), request.getPurpose(), request.getUserId());
        log.info("Generated OTP for {}: {}", request.getTarget(), rawOtp); // Log sanitizer should mask this eventually
        return ResponseEntity.ok(Map.of("message", "OTP generated")); // Ideally we don't return rawOtp here
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<Void> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        otpService.verifyOtp(request.getPublicId(), request.getTarget(), request.getPurpose(), request.getOtp());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody com.smartspace.auth.dto.ResetPasswordRequest request) {
        authService.resetPassword(request.getTarget(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(false) // Local faculty-demo runs over HTTP; enable true behind HTTPS
                .path("/api/v1/auth") // Available to refresh and logout endpoints
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(false)
                .path("/api/v1/auth")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
