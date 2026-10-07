package com.smartspace.booking.service;

import com.smartspace.booking.dto.AddCohostRequest;
import com.smartspace.booking.dto.CohostResponse;
import com.smartspace.booking.entity.Booking;
import com.smartspace.booking.entity.BookingCohost;
import com.smartspace.booking.repository.BookingCohostRepository;
import com.smartspace.booking.repository.BookingRepository;
import com.smartspace.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SplitPaymentService {

    private final BookingRepository bookingRepository;
    private final BookingCohostRepository cohostRepository;
    private final Clock clock;

    @Transactional
    public CohostResponse addCohost(String bookingPublicId, AddCohostRequest request) {
        Booking booking = bookingRepository.findByPublicId(bookingPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getStatus() != com.smartspace.booking.entity.BookingStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Booking must be in PENDING state to add co-hosts");
        }

        BookingCohost cohost = BookingCohost.builder()
                .booking(booking)
                .displayName(request.getDisplayName())
                .phone(request.getPhone())
                .shareAmount(request.getShareAmount())
                .payToken(UUID.randomUUID().toString().replace("-", ""))
                .status(BookingCohost.CohostStatus.INVITED)
                .build();

        cohost = cohostRepository.save(cohost);
        return CohostResponse.fromEntity(cohost);
    }

    @Transactional(readOnly = true)
    public List<CohostResponse> getCohosts(String bookingPublicId) {
        Booking booking = bookingRepository.findByPublicId(bookingPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        return cohostRepository.findByBookingId(booking.getId()).stream()
                .map(CohostResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void mockPayCohost(String payToken) {
        BookingCohost cohost = cohostRepository.findByPayToken(payToken)
                .orElseThrow(() -> new ResourceNotFoundException("Cohost pay token not found"));

        if (cohost.getStatus() == BookingCohost.CohostStatus.PAID) {
            throw new IllegalStateException("Cohost has already paid");
        }
        
        cohost.setStatus(BookingCohost.CohostStatus.PAID);
        cohost.setPaidAt(Instant.now(clock));
        cohostRepository.save(cohost);
        
        // MVP logic: Check if all co-hosts have paid, and if the main lock is still valid, 
        // we might confirm the booking. For MVP, we just update the cohost state.
        // In a real flow, the last person to pay would trigger the Booking status update to CONFIRMED.
    }
}
