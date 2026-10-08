package com.smartspace.entry.controller;

import com.smartspace.booking.entity.Booking;
import com.smartspace.booking.entity.BookingStatus;
import com.smartspace.booking.repository.BookingRepository;
import com.smartspace.booking.service.BookingService;
import com.smartspace.entry.entity.EntryCredential;
import com.smartspace.entry.repository.EntryCredentialRepository;
import com.smartspace.entry.service.EntryService;
import com.smartspace.entry.service.EvidenceService;
import com.smartspace.entry.service.QrKeyGeneratorService;
import com.smartspace.entry.service.QrTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

import org.springframework.security.access.prepost.PreAuthorize;
import com.smartspace.security.auth.SecurityUtils;

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
    private final EvidenceService evidenceService;

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
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<Map<String, String>> getBookingQr(@PathVariable String id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Booking booking = bookingService.getBookingByPublicId(id);
        
        if (!booking.getRenter().getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        
        List<EntryCredential> creds = entryCredentialRepository.findAll();
        EntryCredential credential = creds.stream()
                .filter(c -> c.getBooking().getId().equals(booking.getId()) && c.getKind().equals("HOLDER") && c.getRevokedAt() == null)
                .findFirst()
                .orElse(null);
                
        if (credential == null) {
            return ResponseEntity.notFound().build();
        }
        
        String token = qrTokenService.generateToken(credential);
        return ResponseEntity.ok(Collections.singletonMap("token", token));
    }

    @PostMapping("/scan")
    @PreAuthorize("hasRole('WATCHMAN')")
    public ResponseEntity<Map<String, Object>> scanQr(@RequestBody Map<String, Object> req) {
        Long watchmanUserId = SecurityUtils.getCurrentUserId();
        Long watchmanHallId = getWatchmanHallId(watchmanUserId);

        String token = (String) req.get("token");
        String deviceId = (String) req.get("deviceId");
        
        Map<String, Object> result = entryService.scan(token, watchmanHallId, watchmanUserId, deviceId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/verify-otp")
    @PreAuthorize("hasRole('WATCHMAN')")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody Map<String, Object> req) {
        Long watchmanUserId = SecurityUtils.getCurrentUserId();
        Long watchmanHallId = getWatchmanHallId(watchmanUserId);

        String challengeId = (String) req.get("challengeId");
        String otp = (String) req.get("otp");
        Integer arrivedCount = req.get("arrivedCount") != null ? (Integer) req.get("arrivedCount") : null;
        String deviceId = (String) req.get("deviceId");
        
        Map<String, Object> result = entryService.verifyOtp(challengeId, otp, arrivedCount, watchmanHallId, watchmanUserId, deviceId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/today")
    @PreAuthorize("hasRole('WATCHMAN')")
    public ResponseEntity<List<Map<String, Object>>> getTodayBookings() {
        Long watchmanUserId = SecurityUtils.getCurrentUserId();
        Long watchmanHallId = getWatchmanHallId(watchmanUserId);
        
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        
        List<Booking> bookings = bookingRepository.findAll().stream()
                .filter(b -> b.getHall().getId().equals(watchmanHallId))
                .filter(b -> b.getStartAt().isAfter(startOfDay.toInstant(ZoneOffset.UTC)) && b.getStartAt().isBefore(endOfDay.toInstant(ZoneOffset.UTC)))
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.CHECKED_IN)
                .collect(Collectors.toList());
                
        List<Map<String, Object>> response = bookings.stream().map(b -> {
            Integer currentHeadcount = 0;
            String alertLevel = "OK";
            if (b.getStatus() == BookingStatus.CHECKED_IN) {
                // To avoid N+1 optimally we'd fetch this differently, but for MVP:
                com.smartspace.entry.entity.HallLiveStatus hls = entryService.getHallLiveStatus(b.getHall().getId());
                if (hls != null && hls.getCurrentBooking() != null && hls.getCurrentBooking().getId().equals(b.getId())) {
                    currentHeadcount = hls.getCurrentHeadcount();
                    alertLevel = hls.getCapacityAlertLevel();
                }
            }
            return Map.<String, Object>of(
                "id", b.getId(),
                "bookingRef", b.getBookingRef(),
                "status", b.getStatus().name(),
                "startAt", b.getStartAt(),
                "endAt", b.getEndAt(),
                "displayName", b.getRenter().getFullName(),
                "guestCount", b.getGuestCount(),
                "currentHeadcount", currentHeadcount,
                "capacity", b.getHall().getCapacityStanding(),
                "alertLevel", alertLevel != null ? alertLevel : "OK"
            );
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/handover/before")
    @PreAuthorize("hasRole('WATCHMAN')")
    public ResponseEntity<Void> submitBeforeHandover(@RequestBody com.smartspace.entry.dto.BeforeHandoverRequest request) {
        Long watchmanUserId = SecurityUtils.getCurrentUserId();
        entryService.submitBeforeHandover(request, watchmanUserId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('WATCHMAN')")
    public ResponseEntity<Void> checkoutBooking(@RequestBody com.smartspace.entry.dto.CheckoutRequest request) {
        Long watchmanUserId = SecurityUtils.getCurrentUserId();
        entryService.checkoutBooking(request, watchmanUserId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/headcount")
    @PreAuthorize("hasRole('WATCHMAN')")
    public ResponseEntity<Map<String, Object>> updateHeadcount(@RequestBody com.smartspace.entry.dto.HeadcountRequest request) {
        Long watchmanUserId = SecurityUtils.getCurrentUserId();
        Long watchmanHallId = getWatchmanHallId(watchmanUserId);
        Map<String, Object> result = entryService.updateHeadcount(request.getBookingId(), request.getCount(), watchmanHallId, watchmanUserId, request.getDeviceId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/bookings/{id}/evidence.pdf")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> getEvidencePdf(@PathVariable Long id) {
        // Validation logic belongs in the service.
        byte[] pdfBytes = evidenceService.generateEvidencePdf(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "evidence_BK-" + id + ".pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @PostMapping("/bookings/{id}/checkin-photos")
    @PreAuthorize("hasRole('WATCHMAN')")
    public ResponseEntity<Void> uploadCheckinPhotos(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        // Store BEFORE photos with hash logic (omitted for MVP)
        return ResponseEntity.ok().build();
    }

    private Long getWatchmanHallId(Long watchmanUserId) {
        // TODO: Map watchmanUserId to their assigned hall dynamically
        // For MVP, returning 1L assuming Watchman ID 2 is assigned to Hall 1
        return 1L;
    }

    @GetMapping("/manifest")
    @PreAuthorize("hasAnyRole('WATCHMAN', 'HALL_OWNER')")
    public ResponseEntity<Map<String, Object>> getManifest(@RequestParam(required = false) List<Long> hallIds, @RequestParam(defaultValue = "24") int hours) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Long> targetHallIds = hallIds != null && !hallIds.isEmpty() ? hallIds : List.of(getWatchmanHallId(userId));
        Map<String, Object> manifest = entryService.generateManifest(targetHallIds, hours);
        return ResponseEntity.ok(manifest);
    }

    @PostMapping("/sync")
    @PreAuthorize("hasRole('WATCHMAN')")
    public ResponseEntity<com.smartspace.entry.dto.OfflineSyncResponse> syncOfflineEvents(@RequestBody com.smartspace.entry.dto.OfflineSyncRequest req) {
        Long watchmanUserId = SecurityUtils.getCurrentUserId();
        com.smartspace.entry.dto.OfflineSyncResponse response = entryService.processOfflineSync(req, watchmanUserId);
        return ResponseEntity.ok(response);
    }
}
