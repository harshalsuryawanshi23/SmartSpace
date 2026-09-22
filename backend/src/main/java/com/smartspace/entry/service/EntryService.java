package com.smartspace.entry.service;

import com.smartspace.booking.entity.Booking;
import com.smartspace.booking.entity.BookingStatus;
import com.smartspace.booking.repository.BookingRepository;
import com.smartspace.booking.service.BookingStateMachine;
import com.smartspace.entry.entity.EntryCredential;
import com.smartspace.entry.entity.EntryLog;
import com.smartspace.entry.entity.HallLiveStatus;
import com.smartspace.entry.repository.EntryCredentialRepository;
import com.smartspace.entry.repository.EntryLogRepository;
import com.smartspace.entry.repository.HallLiveStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EntryService {

    private final QrTokenService qrTokenService;
    private final OtpGateChallengeService otpGateChallengeService;
    private final BookingRepository bookingRepository;
    private final EntryCredentialRepository entryCredentialRepository;
    private final EntryLogRepository entryLogRepository;
    private final HallLiveStatusRepository hallLiveStatusRepository;
    private final BookingStateMachine bookingStateMachine;
    private final com.smartspace.entry.repository.HandoverReportRepository handoverReportRepository;

    @Transactional
    public Map<String, Object> scan(String token, Long watchmanHallId, Long watchmanUserId, String deviceId) {
        Map<String, Object> response = new HashMap<>();
        
        // 1. Verify QR structurally and cryptographically
        String verificationVerdict = qrTokenService.verifyToken(token, watchmanHallId);
        if (!"OK".equals(verificationVerdict)) {
            logScan(null, watchmanHallId, null, watchmanUserId, "SCAN", "STOP", verificationVerdict, "NONE", deviceId, null);
            response.put("verdict", "STOP");
            response.put("reasonCode", verificationVerdict);
            return response;
        }

        // Token is valid. Get payload data.
        String[] parts = token.split("\\.");
        String payloadB64 = parts[1];
        String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(payloadB64), java.nio.charset.StandardCharsets.UTF_8);
        Map<String, Object> payload;
        try {
            payload = new com.fasterxml.jackson.databind.ObjectMapper().readValue(payloadJson, Map.class);
        } catch (Exception e) {
            response.put("verdict", "STOP");
            response.put("reasonCode", "QR_MALFORMED");
            return response;
        }
        
        String jti = (String) payload.get("j");
        EntryCredential credential = entryCredentialRepository.findByJti(jti).orElseThrow();
        Booking booking = credential.getBooking();
        
        // 2. Check ALREADY_CHECKED_IN for Reentry
        if (booking.getStatus() == BookingStatus.CHECKED_IN) {
            // Re-entry flow
            logScan(booking, watchmanHallId, credential, watchmanUserId, "SCAN", "HOLD", "ALREADY_CHECKED_IN", "NONE", deviceId, null);
            response.put("verdict", "HOLD");
            response.put("reasonCode", "ALREADY_CHECKED_IN");
            response.put("mode", "REENTRY");
            response.put("bookingRef", booking.getBookingRef());
            response.put("displayName", booking.getRenter().getName());
            return response;
        }

        // 3. KYC Check (Simplified for now - assuming VERIFIED)
        // if kyc != VERIFIED -> STOP(KYC_NOT_VERIFIED)

        // 4. Create OTP Gate Challenge
        OtpGateChallengeService.OtpSession challenge = otpGateChallengeService.createChallenge(booking.getId(), booking.getRenter().getPhone());
        
        logScan(booking, watchmanHallId, credential, watchmanUserId, "SCAN", "HOLD", "OTP_REQUIRED", "NONE", deviceId, null);
        
        EntryLog otpSentLog = logScan(booking, watchmanHallId, credential, watchmanUserId, "OTP_SENT", null, null, "NONE", deviceId, null);

        response.put("verdict", "HOLD");
        response.put("reasonCode", "OTP_REQUIRED");
        response.put("bookingRef", booking.getBookingRef());
        response.put("hallName", booking.getHall().getName());
        response.put("displayName", booking.getRenter().getName());
        response.put("guestsExpected", 0); // Can be added to booking entity if needed
        response.put("challengeId", challenge.getChallengeId());
        
        String phone = booking.getRenter().getPhone();
        String maskedPhone = phone.length() > 4 ? "XXXXXX" + phone.substring(phone.length() - 4) : phone;
        response.put("maskedPhone", maskedPhone);
        
        return response;
    }

    @Transactional
    public Map<String, Object> verifyOtp(String challengeId, String otp, Integer arrivedCount, Long watchmanHallId, Long watchmanUserId, String deviceId) {
        Map<String, Object> response = new HashMap<>();
        
        OtpGateChallengeService.OtpSession session = otpGateChallengeService.getSession(challengeId);
        if (session == null) {
            response.put("verdict", "STOP");
            response.put("reasonCode", "OTP_NOT_FOUND");
            return response;
        }

        Booking booking = bookingRepository.findById(session.getBookingId()).orElseThrow();
        
        boolean ok = otpGateChallengeService.verify(challengeId, otp);
        if (!ok) {
            logScan(booking, watchmanHallId, null, watchmanUserId, "OTP_FAILED", "STOP", session.isLocked() ? "OTP_LOCKED" : "OTP_INVALID", "OTP", deviceId, null);
            response.put("verdict", "STOP");
            response.put("reasonCode", session.isLocked() ? "OTP_LOCKED" : "OTP_INVALID");
            return response;
        }

        // Capacity Guard
        if (arrivedCount != null && arrivedCount > booking.getHall().getCapacityStanding()) {
            response.put("verdict", "HOLD");
            response.put("reasonCode", "CAPACITY_EXCEEDED");
            return response;
        }

        // Transition booking to CHECKED_IN
        bookingStateMachine.transition(booking, BookingStatus.CHECKED_IN, com.smartspace.booking.entity.HistoryEventType.CHECKED_IN, com.smartspace.booking.entity.ActorType.WATCHMAN, watchmanUserId, "{\"reason\": \"Watchman verified OTP\"}");
        bookingRepository.save(booking);
        
        // Update Hall Live Status
        HallLiveStatus hallStatus = hallLiveStatusRepository.findById(booking.getHall().getId())
                .orElse(HallLiveStatus.builder().hall(booking.getHall()).build());
        hallStatus.setStatus("OCCUPIED");
        hallStatus.setCurrentBooking(booking);
        hallStatus.setCurrentHeadcount(arrivedCount != null ? arrivedCount : 0);
        hallLiveStatusRepository.save(hallStatus);
        
        // Log OTP_VERIFIED and CHECK_IN
        logScan(booking, watchmanHallId, null, watchmanUserId, "OTP_VERIFIED", null, null, "OTP", deviceId, null);
        logScan(booking, watchmanHallId, null, watchmanUserId, "CHECK_IN", "GO", null, "OTP", deviceId, arrivedCount);
        
        response.put("verdict", "GO");
        response.put("displayName", booking.getRenter().getName());
        response.put("endsAt", booking.getEndAt());
        
        return response;
    }

    @Transactional
    public void submitBeforeHandover(com.smartspace.entry.dto.BeforeHandoverRequest request, Long watchmanUserId) {
        Booking booking = bookingRepository.findById(request.getBookingId()).orElseThrow();
        
        com.smartspace.entry.entity.HandoverReport report = com.smartspace.entry.entity.HandoverReport.builder()
                .booking(booking)
                .phase(com.smartspace.entry.entity.HandoverReport.HandoverPhase.BEFORE)
                .checklist(request.getChecklist())
                .checklistScore(java.math.BigDecimal.ONE) // Simplified score calculation
                .notes(request.getNotes())
                .recordedBy(new com.smartspace.identity.entity.User(watchmanUserId))
                .recordedAt(LocalDateTime.now())
                .build();
                
        handoverReportRepository.save(report);
    }

    @Transactional
    public void checkoutBooking(com.smartspace.entry.dto.CheckoutRequest request, Long watchmanUserId) {
        Booking booking = bookingRepository.findById(request.getBookingId()).orElseThrow();
        
        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new IllegalStateException("Booking is not in CHECKED_IN state");
        }
        
        // 1. Save AFTER handover report
        com.smartspace.entry.entity.HandoverReport report = com.smartspace.entry.entity.HandoverReport.builder()
                .booking(booking)
                .phase(com.smartspace.entry.entity.HandoverReport.HandoverPhase.AFTER)
                .checklist(request.getChecklist())
                .checklistScore(java.math.BigDecimal.ONE) // Simplified score calculation
                .notes(request.getNotes())
                .recordedBy(new com.smartspace.identity.entity.User(watchmanUserId))
                .recordedAt(LocalDateTime.now())
                .build();
        handoverReportRepository.save(report);
                
        // 2. Transition booking to CHECKED_OUT
        bookingStateMachine.transition(booking, BookingStatus.CHECKED_OUT, com.smartspace.booking.entity.HistoryEventType.CHECKED_OUT, com.smartspace.booking.entity.ActorType.WATCHMAN, watchmanUserId, "{\"reason\": \"Watchman checked out\"}");
        bookingRepository.save(booking);
        
        // 3. Update Hall Live Status to CLEANING
        HallLiveStatus hallStatus = hallLiveStatusRepository.findById(booking.getHall().getId()).orElseThrow();
        hallStatus.setStatus("CLEANING");
        hallStatus.setCurrentBooking(null);
        hallStatus.setCurrentHeadcount(0);
        hallLiveStatusRepository.save(hallStatus);
        
        // 4. Log CHECK_OUT
        logScan(booking, booking.getHall().getId(), null, watchmanUserId, "CHECK_OUT", "GO", null, "NONE", "internal", null);
    }

    private EntryLog logScan(Booking booking, Long hallId, EntryCredential credential, Long watchmanUserId, 
                             String eventType, String verdict, String reasonCode, String identityMethod, 
                             String deviceId, Integer headcount) {
        EntryLog entryLog = EntryLog.builder()
                .clientEventId(UUID.randomUUID().toString())
                .booking(booking)
                .hall(booking != null ? booking.getHall() : (hallId != null ? new com.smartspace.hall.entity.Hall(hallId) : null))
                .credential(credential)
                .watchmanUserId(watchmanUserId)
                .eventType(eventType)
                .verdict(verdict)
                .reasonCode(reasonCode)
                .identityMethod(identityMethod)
                .offline(false)
                .deviceId(deviceId)
                .headcount(headcount)
                .occurredAt(LocalDateTime.now())
                .build();
        
        if (entryLog.getHall() != null && entryLog.getHall().getId() != null) {
            return entryLogRepository.save(entryLog);
        }
        return entryLog;
    }

    @Transactional
    public void sweepNoShows() {
        // Find all CONFIRMED bookings where start time was > 2 hours ago
        java.time.Instant twoHoursAgo = java.time.Instant.now().minus(java.time.Duration.ofHours(2));
        java.util.List<Booking> noShows = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED && b.getStartAt().isBefore(twoHoursAgo))
                .collect(java.util.stream.Collectors.toList());
                
        for (Booking b : noShows) {
            log.info("Marking booking {} as NO_SHOW", b.getId());
            bookingStateMachine.transition(b, BookingStatus.NO_SHOW, com.smartspace.booking.entity.HistoryEventType.NO_SHOW, com.smartspace.booking.entity.ActorType.SYSTEM, null, "{\"reason\": \"No show after 2 hours\"}");
            bookingRepository.save(b);
        }
    }

    @Transactional
    public void sweepCleaningHalls() {
        // Find all CLEANING halls where updated_at was > 2 hours ago
        // Since we don't have updated_at easily queryable via a simple findByStatus without a custom query,
        // we'll fetch all CLEANING and check updated_at.
        java.time.LocalDateTime twoHoursAgo = LocalDateTime.now().minusHours(2);
        
        java.util.List<HallLiveStatus> cleaningHalls = hallLiveStatusRepository.findAll().stream()
                .filter(hls -> "CLEANING".equals(hls.getStatus()) && hls.getUpdatedAt() != null && hls.getUpdatedAt().isBefore(twoHoursAgo))
                .collect(java.util.stream.Collectors.toList());
                
        for (HallLiveStatus hls : cleaningHalls) {
            log.info("Marking hall {} as FREE", hls.getHall().getId());
            hls.setStatus("FREE");
            hallLiveStatusRepository.save(hls);
        }
    }
}
