package com.smartspace.booking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartspace.user.entity.User;
import com.smartspace.user.repository.UserRepository;
import com.smartspace.booking.dto.BookingCreateRequest;
import com.smartspace.booking.dto.BookingQuoteRequest;
import com.smartspace.booking.dto.PriceBreakdown;
import com.smartspace.booking.entity.*;
import com.smartspace.booking.repository.BookingRepository;
import com.smartspace.booking.repository.IdempotencyKeyRepository;
import com.smartspace.common.exception.DomainException;
import com.smartspace.listing.entity.Hall;
import com.smartspace.listing.repository.HallRepository;
import com.smartspace.listing.repository.SocietyMemberRepository;
import com.smartspace.listing.entity.SocietyMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final HallRepository hallRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final SocietyMemberRepository societyMemberRepository;
    private final BookingRulesValidator rulesValidator;
    private final PriceCalculator priceCalculator;
    private final SlotService slotService;
    private final AlternativesService alternativesService;
    private final BookingStateMachine stateMachine;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public PriceBreakdown quote(BookingQuoteRequest req, Long userId) {
        Hall hall = hallRepository.findByPublicId(req.getHallId())
                .orElseThrow(() -> new DomainException(com.smartspace.common.exception.ErrorCode.NOT_FOUND, "Hall not found"));
        
        boolean isMember = societyMemberRepository.findById(new com.smartspace.listing.entity.SocietyMemberId(hall.getSocietyId(), userId))
                .map(m -> com.smartspace.listing.entity.SocietyMemberStatus.APPROVED.equals(m.getStatus()))
                .orElse(false);

        rulesValidator.validate(hall, req.getStartAt(), req.getEndAt(), req.getGuestCount(), isMember, Instant.now(clock));
        return priceCalculator.calculate(hall, req.getStartAt(), req.getEndAt(), isMember);
    }

    @Transactional
    public Booking createBooking(BookingCreateRequest req, Long userId) {
        // Idempotency check
        Optional<IdempotencyKey> existingKey = idempotencyKeyRepository.findByIdempotencyKeyAndUserId(req.getIdempotencyKey(), userId);
        if (existingKey.isPresent()) {
            IdempotencyKey key = existingKey.get();
            if (key.getResponseBody() != null) {
                try {
                    return objectMapper.readValue(key.getResponseBody(), Booking.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Error parsing idempotency response", e);
                }
            } else {
                throw new DomainException(com.smartspace.common.exception.ErrorCode.IDEMPOTENCY_CONFLICT, "A booking request is already in progress");
            }
        }

        IdempotencyKey newKey = new IdempotencyKey();
        newKey.setIdempotencyKey(req.getIdempotencyKey());
        newKey.setUserId(userId);
        newKey.setRequestPath("/api/v1/bookings");
        newKey.setLockedAt(Instant.now(clock));
        idempotencyKeyRepository.save(newKey);

        Hall hall = hallRepository.findByPublicId(req.getHallId())
                .orElseThrow(() -> new DomainException(com.smartspace.common.exception.ErrorCode.NOT_FOUND, "Hall not found"));
        User renter = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException(com.smartspace.common.exception.ErrorCode.NOT_FOUND, "User not found"));

        boolean isMember = societyMemberRepository.findById(new com.smartspace.listing.entity.SocietyMemberId(hall.getSocietyId(), userId))
                .map(m -> com.smartspace.listing.entity.SocietyMemberStatus.APPROVED.equals(m.getStatus()))
                .orElse(false);

        rulesValidator.validate(hall, req.getStartAt(), req.getEndAt(), req.getGuestCount(), isMember, Instant.now(clock));
        PriceBreakdown price = priceCalculator.calculate(hall, req.getStartAt(), req.getEndAt(), isMember);

        Booking booking = new Booking();
        booking.setPublicId(UUID.randomUUID().toString());
        booking.setBookingRef("SS-" + Instant.now(clock).toEpochMilli()); // Simple ref for now
        booking.setHall(hall);
        booking.setRenter(renter);
        booking.setEventType(req.getEventType());
        booking.setEventTitle(req.getEventTitle());
        
        try {
            booking.setThemeTags(objectMapper.writeValueAsString(req.getThemeTags()));
        } catch (JsonProcessingException e) {
            booking.setThemeTags("[]");
        }
        
        booking.setGuestCount(req.getGuestCount());
        booking.setStartAt(req.getStartAt());
        booking.setEndAt(req.getEndAt());
        booking.setStatus(BookingStatus.PENDING_PAYMENT); // initial state
        booking.setLockExpiresAt(Instant.now(clock).plus(15, ChronoUnit.MINUTES)); // 15 min lock
        booking.setPriceBase(price.getBasePrice());
        booking.setPriceMemberDiscount(price.getMemberDiscount());
        booking.setPricePlatformFee(price.getPlatformFee());
        booking.setPriceTax(price.getTax());
        booking.setPriceTotal(price.getTotal());
        booking.setCurrency(price.getCurrency());
        booking.setMemberBooking(isMember);
        booking.setCancellationPolicy(com.smartspace.booking.entity.CancellationPolicy.valueOf(hall.getCancellationPolicy().name()));

        booking = bookingRepository.save(booking);

        // Reserve cells - throws SlotUnavailableException if conflict
        try {
            slotService.reserve(hall, req.getStartAt(), req.getEndAt(), booking);
        } catch (Exception e) {
            // Waitlist / Alternatives logic
            java.util.Map<String, Object> altPayload = alternativesService.computeAlternatives(hall.getId(), req.getStartAt(), req.getEndAt(), req.getGuestCount());
            throw new DomainException(com.smartspace.common.exception.ErrorCode.SLOT_UNAVAILABLE, "The slot is no longer available. Alternatives attached.", null, (java.util.List) altPayload.get("alternatives"));
        }

        // Record state transition
        stateMachine.transition(booking, BookingStatus.PENDING_PAYMENT, HistoryEventType.CREATED, ActorType.RENTER, userId, "{}");

        try {
            newKey.setResponseBody(objectMapper.writeValueAsString(booking));
            newKey.setCompletedAt(Instant.now(clock));
            newKey.setResponseStatus(201);
            idempotencyKeyRepository.save(newKey);
        } catch (JsonProcessingException e) {
            // ignore for now
        }

        return booking;
    }

    public List<Booking> getMyBookings(Long userId) {
        return bookingRepository.findByRenterIdOrderByStartAtDesc(userId);
    }

    public Booking getBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new DomainException(com.smartspace.common.exception.ErrorCode.NOT_FOUND, "Booking not found"));
    }

    public Booking getBookingByPublicId(String publicId) {
        return bookingRepository.findByPublicId(publicId)
                .orElseThrow(() -> new DomainException(com.smartspace.common.exception.ErrorCode.NOT_FOUND, "Booking not found"));
    }
}
