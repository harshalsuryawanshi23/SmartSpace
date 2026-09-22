package com.smartspace.booking.service;

import com.smartspace.booking.dto.SlotRequest;
import com.smartspace.booking.entity.Booking;
import com.smartspace.booking.repository.BookingRepository;
import com.smartspace.common.exception.SlotUnavailableException;
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

    private Long testHallId = 100L;
    private Long testUserId = 200L;
    private Instant todayNoon;

    @BeforeEach
    void setUp() {
        // Setup a fixed reference time (e.g., today at 12:00 PM IST)
        todayNoon = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"))
                .withHour(12).withMinute(0).withSecond(0).withNano(0)
                .toInstant();
    }

    @Test
    void testMultipleConsecutiveBookingsWithBuffer() {
        // First booking: 12:00 PM to 2:00 PM
        SlotRequest request1 = new SlotRequest();
        request1.setHallId(testHallId);
        request1.setStartAt(todayNoon);
        request1.setEndAt(todayNoon.plusSeconds(7200)); // 2 hours
        request1.setIdempotencyKey(UUID.randomUUID().toString());
        request1.setUserId(testUserId);

        Booking booking1 = slotService.reserve(request1);
        assertNotNull(booking1.getId());

        // Assuming a standard 30-minute trailing buffer is enforced by SlotService,
        // a second booking starting at 2:00 PM should FAIL due to the buffer overlap.
        SlotRequest request2 = new SlotRequest();
        request2.setHallId(testHallId);
        request2.setStartAt(todayNoon.plusSeconds(7200)); // 2:00 PM
        request2.setEndAt(todayNoon.plusSeconds(14400)); // 4:00 PM
        request2.setIdempotencyKey(UUID.randomUUID().toString());
        request2.setUserId(testUserId);

        assertThrows(SlotUnavailableException.class, () -> {
            slotService.reserve(request2);
        });

        // But a booking starting at 2:30 PM should SUCCEED.
        SlotRequest request3 = new SlotRequest();
        request3.setHallId(testHallId);
        request3.setStartAt(todayNoon.plusSeconds(9000)); // 2:30 PM (2 hrs + 30 mins buffer = 9000s)
        request3.setEndAt(todayNoon.plusSeconds(16200)); // 4:30 PM
        request3.setIdempotencyKey(UUID.randomUUID().toString());
        request3.setUserId(testUserId);

        Booking booking3 = slotService.reserve(request3);
        assertNotNull(booking3.getId());

        // Verify that 2 bookings exist in the repository for this hall
        List<Booking> bookings = bookingRepository.findAll();
        long count = bookings.stream().filter(b -> b.getHallId().equals(testHallId)).count();
        assertEquals(2, count, "There should be exactly 2 non-overlapping bookings (buffer accounted for)");
    }
}
