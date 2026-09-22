package com.smartspace.entry.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartspace.booking.entity.Booking;
import com.smartspace.booking.entity.BookingStatus;
import com.smartspace.entry.entity.EntryCredential;
import com.smartspace.entry.repository.EntryCredentialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QrTokenService {

    private final QrKeyGeneratorService keyGeneratorService;
    private final EntryCredentialRepository credentialRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Transactional
    public String issueHolderCredential(Booking booking) {
        String jti = UUID.randomUUID().toString().replace("-", "");
        
        // HOLDER window: start - 30 min, end + 15 min buffer (simplified for now)
        LocalDateTime validFrom = booking.getStartAt().atZone(ZoneOffset.UTC).minusMinutes(30).toLocalDateTime();
        LocalDateTime validUntil = booking.getEndAt().atZone(ZoneOffset.UTC).plusMinutes(15).toLocalDateTime();

        EntryCredential credential = EntryCredential.builder()
                .jti(jti)
                .booking(booking)
                .kind("HOLDER")
                .validFrom(validFrom)
                .validUntil(validUntil)
                .keyId(keyGeneratorService.getKeyId())
                .build();
        
        credentialRepository.save(credential);

        return generateToken(credential);
    }

    public String generateToken(EntryCredential credential) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("v", 1);
            payload.put("j", credential.getJti());
            payload.put("k", credential.getKind().substring(0, 1)); // 'H' or 'D'
            payload.put("f", credential.getValidFrom().toEpochSecond(ZoneOffset.UTC));
            payload.put("x", credential.getValidUntil().toEpochSecond(ZoneOffset.UTC));
            payload.put("i", credential.getKeyId());

            String payloadJson = objectMapper.writeValueAsString(payload);
            String payloadB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
            
            String signInput = "SS1." + payloadB64;
            
            Signature signature = Signature.getInstance("Ed25519");
            signature.initSign(keyGeneratorService.getPrivateKey());
            signature.update(signInput.getBytes(StandardCharsets.UTF_8));
            byte[] sigBytes = signature.sign();
            
            String sigB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(sigBytes);
            
            return signInput + "." + sigB64;
        } catch (Exception e) {
            log.error("Failed to generate QR token", e);
            throw new RuntimeException("QR generation failed", e);
        }
    }

    @Transactional(readOnly = true)
    public String verifyToken(String tokenStr, Long watchmanHallId) {
        // 1. Format `SS1.` + 3 parts
        if (tokenStr == null || !tokenStr.startsWith("SS1.")) {
            return "QR_MALFORMED";
        }
        
        String[] parts = tokenStr.split("\\.");
        if (parts.length != 3) {
            return "QR_MALFORMED";
        }
        
        String payloadB64 = parts[1];
        String sigB64 = parts[2];
        
        try {
            String payloadJson = new String(Base64.getUrlDecoder().decode(payloadB64), StandardCharsets.UTF_8);
            Map<String, Object> payload = objectMapper.readValue(payloadJson, Map.class);
            
            String kid = (String) payload.get("i");
            // 2. i (key id) known
            if (!keyGeneratorService.getKeyId().equals(kid)) {
                return "QR_UNKNOWN_KEY";
            }
            
            // 3. Ed25519 signature valid
            String signInput = "SS1." + payloadB64;
            Signature signature = Signature.getInstance("Ed25519");
            signature.initVerify(keyGeneratorService.getPublicKey());
            signature.update(signInput.getBytes(StandardCharsets.UTF_8));
            
            byte[] sigBytes = Base64.getUrlDecoder().decode(sigB64);
            if (!signature.verify(sigBytes)) {
                return "QR_INVALID_SIGNATURE";
            }
            
            String jti = (String) payload.get("j");
            
            // 4. Credential by jti exists
            Optional<EntryCredential> credOpt = credentialRepository.findByJti(jti);
            if (credOpt.isEmpty()) {
                return "QR_UNKNOWN";
            }
            
            EntryCredential credential = credOpt.get();
            
            // 5. Not revoked
            if (credential.getRevokedAt() != null) {
                return "QR_REVOKED";
            }
            
            Booking booking = credential.getBooking();
            
            // 6. Booking status
            if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.CHECKED_IN) {
                if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.CANCELLED_AUTO) {
                    return "BOOKING_CANCELLED";
                }
                return "BOOKING_NOT_ACTIVE";
            }
            
            // 7. Watchman hall auth
            if (watchmanHallId != null && !booking.getHall().getId().equals(watchmanHallId)) {
                return "WRONG_HALL";
            }
            
            // 8. Time window
            LocalDateTime now = LocalDateTime.ofInstant(Instant.now(clock), ZoneOffset.UTC);
            if (now.isBefore(credential.getValidFrom())) {
                return "QR_NOT_YET_VALID";
            }
            if (now.isAfter(credential.getValidUntil())) {
                return "QR_EXPIRED";
            }
            
            return "OK";
        } catch (Exception e) {
            log.error("Error verifying token", e);
            return "QR_MALFORMED";
        }
    }
}
