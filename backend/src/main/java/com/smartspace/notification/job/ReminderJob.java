package com.smartspace.notification.job;

import com.smartspace.booking.entity.Booking;
import com.smartspace.booking.entity.BookingStatus;
import com.smartspace.booking.repository.BookingRepository;
import com.smartspace.notification.entity.NotificationChannel;
import com.smartspace.notification.service.NotificationDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderJob {

    private final BookingRepository bookingRepository;
    private final NotificationDispatcher dispatcher;
    private final Clock clock;

    // Run every 5 minutes
    @Scheduled(fixedDelay = 300000)
    public void processReminders() {
        Instant now = Instant.now(clock);
        
        // 24h reminders (bookings starting between 23h50m and 24h10m from now)
        Instant start24hMin = now.plus(23, ChronoUnit.HOURS).plus(50, ChronoUnit.MINUTES);
        Instant start24hMax = now.plus(24, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES);
        
        List<Booking> bookings24h = bookingRepository.findByStatusAndStartAtBetween(
                BookingStatus.CONFIRMED, start24hMin, start24hMax
        );
        
        for (Booking b : bookings24h) {
            String dedupeKey = "REMINDER_24H:" + b.getId();
            // We use the renter's email
            String email = b.getRenter().getEmail();
            if (email != null) {
                dispatcher.enqueue(
                        b.getRenter().getId(),
                        "REMINDER_24H",
                        "en",
                        email,
                        Map.of("bookingId", b.getId(), "hallId", b.getHall().getId(), "startAt", b.getStartAt().toString()),
                        dedupeKey,
                        List.of(NotificationChannel.EMAIL)
                );
            }
        }

        // 2h reminders (bookings starting between 1h50m and 2h10m from now)
        Instant start2hMin = now.plus(1, ChronoUnit.HOURS).plus(50, ChronoUnit.MINUTES);
        Instant start2hMax = now.plus(2, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES);

        List<Booking> bookings2h = bookingRepository.findByStatusAndStartAtBetween(
                BookingStatus.CONFIRMED, start2hMin, start2hMax
        );

        for (Booking b : bookings2h) {
            String dedupeKey = "REMINDER_2H:" + b.getId();
            String phone = b.getRenter().getPhone();
            if (phone != null) {
                dispatcher.enqueue(
                        b.getRenter().getId(),
                        "REMINDER_2H",
                        "en",
                        phone,
                        Map.of("bookingId", b.getId(), "hallId", b.getHall().getId(), "startAt", b.getStartAt().toString()),
                        dedupeKey,
                        List.of(NotificationChannel.SMS, NotificationChannel.IN_APP)
                );
            }
        }
    }
}
