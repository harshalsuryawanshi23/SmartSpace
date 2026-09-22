package com.smartspace.notification.job;

import com.smartspace.booking.entity.Booking;
import com.smartspace.booking.entity.BookingStatus;
import com.smartspace.booking.repository.BookingRepository;
import com.smartspace.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WrapUpOverstayJob {

    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 * * * * *") // Every minute
    public void checkWrapUpAndOverstay() {
        Instant now = Instant.now();
        
        List<Booking> activeBookings = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.CHECKED_IN)
                .toList();

        for (Booking b : activeBookings) {
            Instant endAt = b.getEndAt();
            
            // 1. WRAP_UP_15
            Instant wrapUpStart = endAt.minus(15, ChronoUnit.MINUTES);
            Instant wrapUpEnd = wrapUpStart.plus(1, ChronoUnit.MINUTES);
            if (now.isAfter(wrapUpStart) && now.isBefore(wrapUpEnd)) {
                notificationService.notifyUser(b.getRenter().getId(), "WRAP_UP_15", 
                        "Your slot for " + b.getEventTitle() + " ends in 15 minutes. Please wrap up music and clean up.", "HIGH");
                log.info("Sent WRAP_UP_15 for booking {}", b.getId());
            }

            // 2. SLOT_ENDED
            Instant slotEndedEnd = endAt.plus(1, ChronoUnit.MINUTES);
            if (now.isAfter(endAt) && now.isBefore(slotEndedEnd)) {
                notificationService.notifyUser(b.getRenter().getId(), "SLOT_ENDED", 
                        "Your slot for " + b.getEventTitle() + " has ended. Please check out immediately to avoid overstay fees.", "HIGH");
                log.info("Sent SLOT_ENDED for booking {}", b.getId());
            }

            // 3. OVERSTAY
            Instant overstayThreshold = endAt.plus(10, ChronoUnit.MINUTES);
            if (now.isAfter(overstayThreshold)) {
                boolean shouldAlert = false;
                if (b.getLastOverstayAlertAt() == null) {
                    shouldAlert = true;
                } else if (now.isAfter(b.getLastOverstayAlertAt().plus(15, ChronoUnit.MINUTES))) {
                    shouldAlert = true;
                }
                
                if (shouldAlert) {
                    notificationService.notifyUser(b.getRenter().getId(), "OVERSTAY", 
                            "Overstay alert: Your event is past the end time. Overstay penalty may apply.", "HIGH");
                    b.setLastOverstayAlertAt(now);
                    bookingRepository.save(b);
                    log.info("Sent OVERSTAY for booking {}", b.getId());
                }
            }
        }
    }
}
