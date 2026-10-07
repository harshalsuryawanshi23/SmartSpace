package com.smartspace.booking.controller;

import com.smartspace.booking.dto.BookingCreateRequest;
import com.smartspace.booking.dto.BookingQuoteRequest;
import com.smartspace.booking.dto.BookingResponse;
import com.smartspace.booking.dto.PriceBreakdown;
import com.smartspace.booking.entity.Booking;
import com.smartspace.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import com.smartspace.security.auth.SecurityUtils;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final com.smartspace.booking.service.CancellationService cancellationService;
    @PostMapping("/quote")
    @PreAuthorize("hasRole('RENTER')")
    public ResponseEntity<PriceBreakdown> quote(@RequestBody BookingQuoteRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(bookingService.quote(request, userId));
    }

    @PostMapping
    @PreAuthorize("hasRole('RENTER')")
    public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Booking booking = bookingService.createBooking(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(booking));
    }

    @GetMapping
    @PreAuthorize("hasRole('RENTER')")
    public ResponseEntity<List<BookingResponse>> getMyBookings() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<BookingResponse> responses = bookingService.getMyBookings(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('RENTER')")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Booking booking = bookingService.getBooking(id);
        if (!booking.getRenter().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(mapToResponse(booking));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('RENTER')")
    public ResponseEntity<?> cancelBooking(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean preview,
            @RequestBody(required = false) String reason) {
        Long userId = SecurityUtils.getCurrentUserId();
        Booking booking = bookingService.getBooking(id);
        
        if (!booking.getRenter().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        if (preview) {
            return ResponseEntity.ok(cancellationService.previewRefund(booking, com.smartspace.booking.entity.ActorType.RENTER));
        }
        
        booking = cancellationService.cancelBooking(booking, com.smartspace.booking.entity.ActorType.RENTER, userId, reason);
        return ResponseEntity.ok(mapToResponse(booking));
    }

    private BookingResponse mapToResponse(Booking booking) {
        BookingResponse r = new BookingResponse();
        r.setPublicId(booking.getPublicId());
        r.setBookingRef(booking.getBookingRef());
        r.setHallId(booking.getHall().getId());
        r.setRenterUserId(booking.getRenter().getId());
        r.setEventType(booking.getEventType());
        r.setEventTitle(booking.getEventTitle());
        r.setGuestCount(booking.getGuestCount());
        r.setStartAt(booking.getStartAt());
        r.setEndAt(booking.getEndAt());
        r.setStatus(booking.getStatus());
        r.setLockExpiresAt(booking.getLockExpiresAt());
        r.setPriceTotal(booking.getPriceTotal());
        r.setCurrency(booking.getCurrency());
        r.setCancellationPolicy(booking.getCancellationPolicy());
        r.setCreatedAt(booking.getCreatedAt());
        return r;
    }
}
