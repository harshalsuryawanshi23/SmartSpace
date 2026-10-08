package com.smartspace.booking.service;

import com.smartspace.booking.entity.*;
import com.smartspace.booking.repository.BookingEventRepository;
import com.smartspace.booking.repository.BookingRepository;
import com.smartspace.common.exception.DomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingStateMachine {

    private final BookingRepository bookingRepository;
    private final BookingEventRepository bookingEventRepository;

    @Transactional
    public Booking transition(Booking booking, BookingStatus targetStatus, HistoryEventType eventType, ActorType actorType, Long actorId, String detailsJson) {
        validateTransition(booking.getStatus(), targetStatus);

        booking.setStatus(targetStatus);
        booking = bookingRepository.save(booking);

        BookingEvent event = new BookingEvent();
        event.setBooking(booking);
        event.setEventType(eventType);
        event.setActorType(actorType);
        event.setActorId(actorId);
        event.setDetails(detailsJson);
        bookingEventRepository.save(event);

        // TODO: enqueue notifications via Outbox
        return booking;
    }

    private void validateTransition(BookingStatus current, BookingStatus target) {
        boolean valid = false;
        switch (target) {
            case PENDING_PAYMENT:
                valid = (current == null || current == BookingStatus.PENDING_PAYMENT);
                break;
            case CONFIRMED:
                valid = (current == BookingStatus.PENDING_PAYMENT);
                break;
            case EXPIRED:
                valid = (current == BookingStatus.PENDING_PAYMENT);
                break;
            case CANCELLED:
                valid = Arrays.asList(BookingStatus.PENDING_PAYMENT, BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN).contains(current);
                break;
            case CHECKED_IN:
            case NO_SHOW:
                valid = (current == BookingStatus.CONFIRMED);
                break;
            case CHECKED_OUT:
                valid = (current == BookingStatus.CHECKED_IN);
                break;
            case COMPLETED:
                valid = (current == BookingStatus.CHECKED_OUT);
                break;
        }

        if (!valid) {
            throw new DomainException(com.smartspace.common.exception.ErrorCode.ILLEGAL_STATE_TRANSITION, "Cannot transition booking from " + current + " to " + target);
        }
    }
}
