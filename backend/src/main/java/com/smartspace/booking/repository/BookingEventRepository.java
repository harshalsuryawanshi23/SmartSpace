package com.smartspace.booking.repository;

import com.smartspace.booking.entity.BookingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingEventRepository extends JpaRepository<BookingEvent, Long> {
    List<BookingEvent> findByBookingIdOrderByCreatedAtAsc(Long bookingId);
}
