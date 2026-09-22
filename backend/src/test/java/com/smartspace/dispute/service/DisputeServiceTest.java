package com.smartspace.dispute.service;

import com.smartspace.booking.entity.Booking;
import com.smartspace.booking.entity.BookingStatus;
import com.smartspace.booking.repository.BookingRepository;
import com.smartspace.common.exception.ResourceNotFoundException;
import com.smartspace.dispute.dto.DisputeRaiseRequest;
import com.smartspace.dispute.entity.Dispute;
import com.smartspace.dispute.entity.DisputeCategory;
import com.smartspace.dispute.entity.DisputeSide;
import com.smartspace.dispute.repository.DisputeEvidenceRepository;
import com.smartspace.dispute.repository.DisputeRepository;
import com.smartspace.user.entity.User;
import com.smartspace.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisputeServiceTest {

    @Mock
    private DisputeRepository disputeRepository;
    @Mock
    private DisputeEvidenceRepository disputeEvidenceRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;

    private Clock clock;
    private DisputeService disputeService;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2024-01-10T12:00:00Z"), ZoneId.of("UTC"));
        disputeService = new DisputeService(
                disputeRepository,
                disputeEvidenceRepository,
                bookingRepository,
                userRepository,
                clock
        );
    }

    @Test
    void raiseDispute_Success() {
        // Arrange
        Long userId = 1L;
        DisputeRaiseRequest request = new DisputeRaiseRequest();
        request.setBookingId(10L);
        request.setAgainstSide(DisputeSide.RENTER);
        request.setCategory(DisputeCategory.DAMAGE);
        request.setDescription("Broken chair");
        request.setClaimedAmount(new BigDecimal("5000.00"));

        Booking booking = new Booking();
        booking.setId(10L);
        booking.setStatus(BookingStatus.CHECKED_OUT);
        // Checked out 24 hours ago (well within 72h)
        booking.setEndAt(Instant.now(clock).minus(24, ChronoUnit.HOURS));

        User user = new User();
        user.setId(userId);

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(disputeRepository.save(any(Dispute.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Dispute dispute = disputeService.raiseDispute(userId, request);

        // Assert
        assertNotNull(dispute);
        assertEquals(DisputeSide.RENTER, dispute.getAgainstSide());
        assertEquals(DisputeCategory.DAMAGE, dispute.getCategory());
        assertEquals(booking, dispute.getBooking());
        assertEquals(user, dispute.getRaisedBy());
        verify(disputeRepository).save(any(Dispute.class));
    }

    @Test
    void raiseDispute_FailsWhenBookingNotCompletedOrCheckedOut() {
        // Arrange
        Long userId = 1L;
        DisputeRaiseRequest request = new DisputeRaiseRequest();
        request.setBookingId(10L);

        Booking booking = new Booking();
        booking.setId(10L);
        booking.setStatus(BookingStatus.CONFIRMED); // Not checked out

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        // Act & Assert
        Exception exception = assertThrows(IllegalStateException.class, () -> 
            disputeService.raiseDispute(userId, request)
        );
        assertTrue(exception.getMessage().contains("must be completed or checked out"));
    }

    @Test
    void raiseDispute_FailsWhenBeyond72Hours() {
        // Arrange
        Long userId = 1L;
        DisputeRaiseRequest request = new DisputeRaiseRequest();
        request.setBookingId(10L);

        Booking booking = new Booking();
        booking.setId(10L);
        booking.setStatus(BookingStatus.CHECKED_OUT);
        // Checked out 80 hours ago
        booking.setEndAt(Instant.now(clock).minus(80, ChronoUnit.HOURS));

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        // Act & Assert
        Exception exception = assertThrows(IllegalStateException.class, () -> 
            disputeService.raiseDispute(userId, request)
        );
        assertTrue(exception.getMessage().contains("72 hours"));
    }
}
