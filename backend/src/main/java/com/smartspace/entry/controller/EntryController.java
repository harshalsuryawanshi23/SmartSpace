package com.smartspace.entry.controller;

import com.smartspace.booking.entity.Booking;
import com.smartspace.booking.entity.BookingStatus;
import com.smartspace.booking.repository.BookingRepository;
import com.smartspace.booking.service.BookingService;
import com.smartspace.entry.entity.EntryCredential;
import com.smartspace.entry.repository.EntryCredentialRepository;
import com.smartspace.entry.service.EntryService;
import com.smartspace.entry.service.QrKeyGeneratorService;
import com.smartspace.entry.service.QrTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/entry")
@RequiredArgsConstructor
public class EntryController {

    private final QrKeyGeneratorService keyGeneratorService;
    private final QrTokenService qrTokenService;
    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final EntryCredentialRepository entryCredentialRepository;
    private final EntryService entryService;

    @GetMapping("/public-keys")
    public ResponseEntity<Map<String, List<Map<String, String>>>> getPublicKeys() {
        byte[] rawKey = keyGeneratorService.getPublicKey().getEncoded();
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

    // Moved from /bookings/{id}/qr to /entry/bookings/{id}/qr for path consistency
    // Although standard would be to keep it under bookings. Let's create an alias here just in case.
    @GetMapping("/bookings/{id}/qr")
    public ResponseEntity<Map<String, String>> getBookingQr(@PathVariable Long id) {
        Long userId = 1L; // TODO: SecurityContext
        Booking booking = bookingService.getBooking(id);
        
        if (!booking.getRenter().getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        
        List<EntryCredential> creds = entryCredentialRepository.findAll();
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

    @PostMapping("/scan")
    public ResponseEntity<Map<String, Object>> scanQr(@RequestBody Map<String, Object> req) {
        Long watchmanUserId = 2L; // TODO: SecurityContext
        Long watchmanHallId = 1L; // TODO: Watchman's assigned hall

        String token = (String) req.get("token");
        String deviceId = (String) req.get("deviceId");
        
        Map<String, Object> result = entryService.scan(token, watchmanHallId, watchmanUserId, deviceId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody Map<String, Object> req) {
        Long watchmanUserId = 2L; // TODO: SecurityContext
        Long watchmanHallId = 1L; // TODO: Watchman's assigned hall

        String challengeId = (String) req.get("challengeId");
        String otp = (String) req.get("otp");
        Integer arrivedCount = req.get("arrivedCount") != null ? (Integer) req.get("arrivedCount") : null;
        String deviceId = (String) req.get("deviceId");
        
        Map<String, Object> result = entryService.verifyOtp(challengeId, otp, arrivedCount, watchmanHallId, watchmanUserId, deviceId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/today")
    public ResponseEntity<List<Map<String, Object>>> getTodayBookings() {
        Long watchmanHallId = 1L; // TODO: SecurityContext
        
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        
        List<Booking> bookings = bookingRepository.findAll().stream()
                .filter(b -> b.getHall().getId().equals(watchmanHallId))
                .filter(b -> b.getStartAt().isAfter(startOfDay.toInstant(ZoneOffset.UTC)) && b.getStartAt().isBefore(endOfDay.toInstant(ZoneOffset.UTC)))
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.CHECKED_IN)
                .collect(Collectors.toList());
                
        List<Map<String, Object>> response = bookings.stream().map(b -> Map.of(
                "id", b.getId(),
                "bookingRef", b.getBookingRef(),
                "status", b.getStatus().name(),
                "startAt", b.getStartAt(),
                "endAt", b.getEndAt(),
                "displayName", b.getRenter().getName()
        )).collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/handover/before")
    public ResponseEntity<Void> submitBeforeHandover(@RequestBody com.smartspace.entry.dto.BeforeHandoverRequest request) {
        Long watchmanUserId = 2L; // TODO: SecurityContext
        entryService.submitBeforeHandover(request, watchmanUserId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/checkout")
    public ResponseEntity<Void> checkoutBooking(@RequestBody com.smartspace.entry.dto.CheckoutRequest request) {
        Long watchmanUserId = 2L; // TODO: SecurityContext
        entryService.checkoutBooking(request, watchmanUserId);
        return ResponseEntity.ok().build();
    }
}
