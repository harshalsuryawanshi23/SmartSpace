package com.smartspace.dispute.service;

import com.smartspace.booking.entity.Booking;
import com.smartspace.booking.entity.BookingStatus;
import com.smartspace.booking.repository.BookingRepository;
import com.smartspace.common.exception.ResourceNotFoundException;
import com.smartspace.dispute.dto.DisputeRaiseRequest;
import com.smartspace.dispute.dto.DisputeResolveRequest;
import com.smartspace.dispute.entity.Dispute;
import com.smartspace.dispute.entity.DisputeEvidence;
import com.smartspace.dispute.repository.DisputeEvidenceRepository;
import com.smartspace.dispute.repository.DisputeRepository;
import com.smartspace.user.entity.User;
import com.smartspace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DisputeService {

    private final DisputeRepository disputeRepository;
    private final DisputeEvidenceRepository disputeEvidenceRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    @Transactional
    public Dispute raiseDispute(Long userId, DisputeRaiseRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
                
        if (booking.getStatus() != BookingStatus.COMPLETED && booking.getStatus() != BookingStatus.CHECKED_OUT) {
            throw new IllegalStateException("Booking must be completed or checked out to raise a dispute");
        }

        // 72-hour constraint
        Instant cutoff = Instant.now(clock).minus(72, ChronoUnit.HOURS);
        if (booking.getEndAt().isBefore(cutoff)) {
            throw new IllegalStateException("Dispute must be raised within 72 hours of check-out");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Dispute dispute = new Dispute();
        dispute.setPublicId(UUID.randomUUID().toString());
        dispute.setBooking(booking);
        dispute.setRaisedBy(user);
        dispute.setAgainstSide(request.getAgainstSide());
        dispute.setCategory(request.getCategory());
        dispute.setDescription(request.getDescription());
        dispute.setClaimedAmount(request.getClaimedAmount());
        dispute.setCreatedAt(Instant.now(clock));

        // Mark booking with dispute_open = true
        // For now, we assume we just save the dispute, the system will check the dispute table to see if it's open
        return disputeRepository.save(dispute);
    }

    @Transactional
    public DisputeEvidence uploadEvidence(Long userId, Long disputeId, String filePath, String note) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        DisputeEvidence evidence = new DisputeEvidence();
        evidence.setDispute(dispute);
        evidence.setSubmittedBy(user);
        evidence.setFilePath(filePath);
        evidence.setNote(note);
        evidence.setCreatedAt(Instant.now(clock));

        return disputeEvidenceRepository.save(evidence);
    }

    @Transactional
    public Dispute resolveDispute(Long adminId, Long disputeId, DisputeResolveRequest request) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute not found"));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        dispute.setStatus(request.getStatus());
        dispute.setResolutionNote(request.getResolutionNote());
        dispute.setResolvedBy(admin);
        dispute.setResolvedAt(Instant.now(clock));

        // Here we could implement hooks for trust penalties and refunds

        return disputeRepository.save(dispute);
    }
}
