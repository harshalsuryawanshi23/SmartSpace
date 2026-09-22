package com.smartspace.booking.service;

import com.smartspace.booking.dto.RefundPreview;
import com.smartspace.booking.entity.ActorType;
import com.smartspace.booking.entity.Booking;
import com.smartspace.booking.entity.BookingStatus;
import com.smartspace.booking.entity.HistoryEventType;
import com.smartspace.booking.repository.BookingRepository;
import com.smartspace.booking.repository.BookingCellRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CancellationService {

    private final BookingRepository bookingRepository;
    private final BookingCellRepository bookingCellRepository;
    private final BookingStateMachine stateMachine;
    private final Clock clock;

    public RefundPreview previewRefund(Booking booking, ActorType actor) {
        BigDecimal refundAmount = BigDecimal.ZERO;
        if (booking.getStatus() == BookingStatus.PENDING_PAYMENT) {
            return new RefundPreview().setExpectedRefund(BigDecimal.ZERO).setPolicyApplied("NONE").setReason("Unpaid booking");
        }

        if (actor == ActorType.OWNER || actor == ActorType.ADMIN || actor == ActorType.SYSTEM) {
            refundAmount = booking.getPriceTotal();
            return new RefundPreview().setExpectedRefund(refundAmount).setPolicyApplied("FULL_REFUND_OWNER_CANCEL").setReason("Cancelled by owner/admin");
        }

        long hoursBeforeStart = Duration.between(Instant.now(clock), booking.getStartAt()).toHours();
        BigDecimal subtotalPlusTax = booking.getPriceBase().subtract(booking.getPriceMemberDiscount()).add(booking.getPriceTax());
        // Simple mock of refund rules
        switch (booking.getCancellationPolicy()) {
            case FLEXIBLE:
                if (hoursBeforeStart >= 24) refundAmount = subtotalPlusTax;
                else if (hoursBeforeStart >= 6) refundAmount = subtotalPlusTax.multiply(new BigDecimal("0.5")).setScale(2, RoundingMode.HALF_UP);
                break;
            case MODERATE:
                if (hoursBeforeStart >= 72) refundAmount = subtotalPlusTax;
                else if (hoursBeforeStart >= 24) refundAmount = subtotalPlusTax.multiply(new BigDecimal("0.5")).setScale(2, RoundingMode.HALF_UP);
                break;
            case STRICT:
                if (hoursBeforeStart >= 168) refundAmount = subtotalPlusTax.multiply(new BigDecimal("0.5")).setScale(2, RoundingMode.HALF_UP);
                break;
        }

        return new RefundPreview().setExpectedRefund(refundAmount).setPolicyApplied(booking.getCancellationPolicy().name()).setReason("Based on time before start");
    }

    @Transactional
    public Booking cancelBooking(Booking booking, ActorType actor, Long actorId, String reason) {
        RefundPreview preview = previewRefund(booking, actor);
        
        booking.setCancelledBy(actor);
        booking.setCancelReason(reason);
        booking.setCancelledAt(Instant.now(clock));
        
        // Transition state
        booking = stateMachine.transition(booking, BookingStatus.CANCELLED, HistoryEventType.CANCELLED, actor, actorId, "{\"reason\": \"" + reason + "\", \"expectedRefund\": " + preview.getExpectedRefund() + "}");
        
        // Release cells
        bookingCellRepository.deleteByBookingId(booking.getId());

        // TODO: Enqueue refund processing if preview.getExpectedRefund() > 0

        return booking;
    }
}
