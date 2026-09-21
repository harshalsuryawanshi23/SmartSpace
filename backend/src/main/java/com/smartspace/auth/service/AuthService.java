package com.smartspace.auth.service;

import com.smartspace.auth.dto.AuthResponse;
import com.smartspace.auth.dto.LoginRequest;
import com.smartspace.auth.dto.RegisterRequest;
import com.smartspace.auth.entity.RefreshToken;
import com.smartspace.auth.jwt.JwtProvider;
import com.smartspace.auth.repository.RefreshTokenRepository;
import com.smartspace.common.exception.DomainException;
import com.smartspace.common.exception.ErrorCode;
import com.smartspace.common.util.IdGenerator;
import com.smartspace.user.entity.User;
import com.smartspace.user.entity.UserRole;
import com.smartspace.user.entity.UserStatus;
import com.smartspace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final Clock clock;

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DomainException(ErrorCode.VALIDATION_ERROR, "Email is already registered");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new DomainException(ErrorCode.VALIDATION_ERROR, "Phone is already registered");
        }

        // Only allow self-registration for certain roles to prevent privilege escalation
        if (request.getRole() == UserRole.ADMIN || request.getRole() == UserRole.WATCHMAN) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Cannot self-register as ADMIN or WATCHMAN");
        }

        User user = User.builder()
                .publicId(IdGenerator.generatePublicId("usr"))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.PENDING) // Needs email/phone verification in a real scenario
                .roles(Set.of(request.getRole()))
                .build();

        userRepository.save(user);
    }
    
    @Transactional
    public void registerWatchman(RegisterRequest request) {
        if ((request.getEmail() == null || request.getEmail().isBlank()) && 
            (request.getPhone() == null || request.getPhone().isBlank())) {
            throw new DomainException(ErrorCode.VALIDATION_ERROR, "Either email or phone is required");
        }

        User user = User.builder()
                .publicId(UUID.randomUUID().toString())
                .fullName(request.getFirstName() + " " + request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(UserRole.WATCHMAN))
                .status(UserStatus.REQUIRE_PASSWORD_CHANGE)
                .build();
        
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(String publicId, String oldPassword, String newPassword) {
        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new DomainException(ErrorCode.VALIDATION_ERROR, "Invalid old password");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        if (user.getStatus() == UserStatus.REQUIRE_PASSWORD_CHANGE) {
            user.setStatus(UserStatus.ACTIVE);
        }
        userRepository.save(user);
        
        // Revoke all sessions for security
        refreshTokenRepository.revokeAllForUser(user.getId(), clock.instant());
    }

    @Transactional
    public void resetPassword(String target, String newPassword) {
        // Here we assume OTP is already verified in a real scenario
        Optional<User> optionalUser = target.contains("@")
                ? userRepository.findByEmail(target)
                : userRepository.findByPhone(target);
                
        User user = optionalUser.orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND, "User not found"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        refreshTokenRepository.revokeAllForUser(user.getId(), clock.instant());
    }

    @Transactional
    public AuthResult login(LoginRequest request, String userAgent, String ip) {
        Optional<User> optionalUser = request.getIdentifier().contains("@")
                ? userRepository.findByEmail(request.getIdentifier())
                : userRepository.findByPhone(request.getIdentifier());

        User user = optionalUser.orElseThrow(() -> new DomainException(ErrorCode.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new DomainException(ErrorCode.UNAUTHORIZED, "Invalid credentials");
        }

        if (user.getStatus() == UserStatus.SUSPENDED || user.getStatus() == UserStatus.DELETED) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Account is suspended or deleted");
        }

        user.setLastLoginAt(clock.instant());
        userRepository.save(user);

        return generateTokens(user, userAgent, ip);
    }

    @Transactional
    public AuthResult refresh(String refreshTokenCookie, String userAgent, String ip) {
        String hash = hashRefreshToken(refreshTokenCookie);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new DomainException(ErrorCode.UNAUTHORIZED, "Invalid refresh token"));

        if (refreshToken.getRevokedAt() != null || refreshToken.getExpiresAt().isBefore(clock.instant())) {
            // Token was revoked or expired. If it was revoked, maybe a replay attack. Revoke family.
            if (refreshToken.getRevokedAt() != null) {
                refreshTokenRepository.revokeFamily(refreshToken.getFamilyId(), clock.instant());
            }
            throw new DomainException(ErrorCode.UNAUTHORIZED, "Invalid refresh token");
        }

        User user = refreshToken.getUser();
        if (user.getStatus() != UserStatus.ACTIVE && user.getStatus() != UserStatus.PENDING && user.getStatus() != UserStatus.REQUIRE_PASSWORD_CHANGE) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Account is not active");
        }

        // Revoke the old token (rotation)
        refreshToken.setRevokedAt(clock.instant());
        
        return generateTokens(user, userAgent, ip, refreshToken.getFamilyId());
    }
    
    @Transactional
    public void logout(String refreshTokenCookie) {
        if (refreshTokenCookie != null && !refreshTokenCookie.isBlank()) {
            String hash = hashRefreshToken(refreshTokenCookie);
            refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
                token.setRevokedAt(clock.instant());
                refreshTokenRepository.save(token);
            });
        }
    }

    private AuthResult generateTokens(User user, String userAgent, String ip) {
        return generateTokens(user, userAgent, ip, UUID.randomUUID().toString());
    }

    private AuthResult generateTokens(User user, String userAgent, String ip, String familyId) {
        List<String> roles = user.getRoles().stream().map(Enum::name).collect(Collectors.toList());
        String accessToken = jwtProvider.generateAccessToken(user.getPublicId(), roles);

        String rawRefreshToken = UUID.randomUUID().toString();
        String hash = hashRefreshToken(rawRefreshToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hash)
                .familyId(familyId)
                .expiresAt(clock.instant().plus(7, ChronoUnit.DAYS))
                .userAgent(userAgent)
                .ip(ip)
                .build();

        refreshTokenRepository.save(refreshToken);

        AuthResponse.UserDto userDto = AuthResponse.UserDto.builder()
                .publicId(user.getPublicId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roles(user.getRoles())
                .build();

        boolean requiresPasswordChange = user.getStatus() == UserStatus.REQUIRE_PASSWORD_CHANGE;

        AuthResponse response = AuthResponse.builder()
                .accessToken(accessToken)
                .user(userDto)
                .requiresPasswordChange(requiresPasswordChange)
                .build();

        return new AuthResult(response, rawRefreshToken);
    }

    private String hashRefreshToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static class AuthResult {
        public final AuthResponse response;
        public final String rawRefreshToken;

        public AuthResult(AuthResponse response, String rawRefreshToken) {
            this.response = response;
            this.rawRefreshToken = rawRefreshToken;
        }
    }
}
