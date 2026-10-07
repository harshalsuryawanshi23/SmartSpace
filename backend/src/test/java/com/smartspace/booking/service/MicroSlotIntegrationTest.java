package com.smartspace.booking.service;

import com.smartspace.booking.entity.Booking;
import com.smartspace.booking.repository.BookingRepository;
import com.smartspace.booking.exception.SlotUnavailableException;
import com.smartspace.listing.entity.Hall;
import com.smartspace.listing.repository.HallRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MicroSlotIntegrationTest {

    @Autowired
    private SlotService slotService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private HallRepository hallRepository;

    private Hall testHall;
    private Instant todayNoon;

    @BeforeEach
    void setUp() {
        todayNoon = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"))
                .withHour(12).withMinute(0).withSecond(0).withNano(0)
                .toInstant();
                
        testHall = Hall.builder()
                .name("Test Hall")
                .bufferAfterMinutes(30)
                .build();
        hallRepository.save(testHall);
    }

    @Test
    void testMultipleConsecutiveBookingsWithBuffer() {
        // First booking: 12:00 PM to 2:00 PM
        Booking booking1 = new Booking();
        booking1.setBookingRef(UUID.randomUUID().toString());
        booking1.setStartAt(todayNoon);
        booking1.setEndAt(todayNoon.plusSeconds(7200)); // 2 hours
        booking1.setHall(testHall);
        booking1.setEventType(com.smartspace.booking.entity.BookingEventType.BIRTHDAY);
        booking1.setEventTitle("Test Event");
        booking1.setGuestCount(50);
        booking1.setPriceBase(java.math.BigDecimal.valueOf(1000));
        booking1.setPriceTotal(java.math.BigDecimal.valueOf(1000));
        booking1.setCancellationPolicy(com.smartspace.booking.entity.CancellationPolicy.FLEXIBLE);
        // We also need to set a Renter user, but for SlotService.reserve it may only need the ID/hall.
        // Let's create and set a dummy renter if needed. 
        // For reserve() we just need booking id in some cases, but bookingRepository.save(booking) needs all nullable=false fields.
        com.smartspace.user.entity.User renter = new com.smartspace.user.entity.User();
        org.springframework.test.util.ReflectionTestUtils.setField(renter, "id", 200L);
        booking1.setRenter(renter);
        bookingRepository.save(booking1);
        
        slotService.reserve(testHall, booking1.getStartAt(), booking1.getEndAt(), booking1);

        // A second booking starting at 2:00 PM should FAIL due to the buffer overlap.
        Booking booking2 = new Booking();
        booking2.setBookingRef(UUID.randomUUID().toString());
        booking2.setStartAt(todayNoon.plusSeconds(7200)); // 2:00 PM
        booking2.setEndAt(todayNoon.plusSeconds(14400)); // 4:00 PM
        booking2.setHall(testHall);
        booking2.setEventType(com.smartspace.booking.entity.BookingEventType.BIRTHDAY);
        booking2.setEventTitle("Test Event");
        booking2.setGuestCount(50);
        booking2.setPriceBase(java.math.BigDecimal.valueOf(1000));
        booking2.setPriceTotal(java.math.BigDecimal.valueOf(1000));
        booking2.setCancellationPolicy(com.smartspace.booking.entity.CancellationPolicy.FLEXIBLE);
        booking2.setRenter(renter);
        bookingRepository.save(booking2);

        assertThrows(SlotUnavailableException.class, () -> {
            slotService.reserve(testHall, booking2.getStartAt(), booking2.getEndAt(), booking2);
        });

        // But a booking starting at 2:30 PM should SUCCEED.
        Booking booking3 = new Booking();
        booking3.setBookingRef(UUID.randomUUID().toString());
        booking3.setStartAt(todayNoon.plusSeconds(9000)); // 2:30 PM (2 hrs + 30 mins buffer = 9000s)
        booking3.setEndAt(todayNoon.plusSeconds(16200)); // 4:30 PM
        booking3.setHall(testHall);
        booking3.setEventType(com.smartspace.booking.entity.BookingEventType.BIRTHDAY);
        booking3.setEventTitle("Test Event");
        booking3.setGuestCount(50);
        booking3.setPriceBase(java.math.BigDecimal.valueOf(1000));
        booking3.setPriceTotal(java.math.BigDecimal.valueOf(1000));
        booking3.setCancellationPolicy(com.smartspace.booking.entity.CancellationPolicy.FLEXIBLE);
        booking3.setRenter(renter);
        bookingRepository.save(booking3);

        slotService.reserve(testHall, booking3.getStartAt(), booking3.getEndAt(), booking3);

        List<Booking> bookings = bookingRepository.findAll();
        long count = bookings.stream().filter(b -> b.getHall().getId().equals(testHall.getId())).count();
        assertEquals(3, count, "There should be exactly 3 bookings (1 failed reservation is still in DB as we saved it before reserve)");
    }
}
