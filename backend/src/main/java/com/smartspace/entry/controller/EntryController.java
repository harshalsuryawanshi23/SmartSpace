package com.smartspace.entry.controller;

import com.smartspace.booking.entity.Booking;
import com.smartspace.booking.service.BookingService;
import com.smartspace.entry.entity.EntryCredential;
import com.smartspace.entry.repository.EntryCredentialRepository;
import com.smartspace.entry.service.QrKeyGeneratorService;
import com.smartspace.entry.service.QrTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EntryController {

    private final QrKeyGeneratorService keyGeneratorService;
    private final QrTokenService qrTokenService;
    private final BookingService bookingService;
    private final EntryCredentialRepository entryCredentialRepository;

    @GetMapping("/entry/public-keys")
    public ResponseEntity<Map<String, List<Map<String, String>>>> getPublicKeys() {
        byte[] rawKey = keyGeneratorService.getPublicKey().getEncoded();
        // The standard X.509 SubjectPublicKeyInfo contains the raw 32-byte Ed25519 key at the very end
        byte[] raw32 = new byte[32];
        System.arraycopy(rawKey, rawKey.length - 32, raw32, 0, 32);
        
        String x = Base64.getUrlEncoder().withoutPadding().encodeToString(raw32);
        
        Map<String, String> keyInfo = Map.of(
                "kid", keyGeneratorService.getKeyId(),
                "alg", "Ed25519",
                "x", x
        );
        
        return ResponseEntity.ok(Map.of("keys", List.of(keyInfo)));
    }

    @GetMapping("/bookings/{id}/qr")
    public ResponseEntity<Map<String, String>> getBookingQr(@PathVariable Long id) {
        Long userId = 1L; // TODO: SecurityContext
        Booking booking = bookingService.getBooking(id);
        
        if (!booking.getRenter().getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        
        List<EntryCredential> creds = entryCredentialRepository.findAll(); // simplified
        EntryCredential credential = creds.stream()
                .filter(c -> c.getBooking().getId().equals(id) && c.getKind().equals("HOLDER") && c.getRevokedAt() == null)
                .findFirst()
                .orElse(null);
                
        if (credential == null) {
            return ResponseEntity.notFound().build();
        }
        
        String token = qrTokenService.generateToken(credential);
        return ResponseEntity.ok(Collections.singletonMap("token", token));
    }
}
