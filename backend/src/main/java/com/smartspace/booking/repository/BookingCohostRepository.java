package com.smartspace.booking.repository;

import com.smartspace.booking.entity.BookingCohost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingCohostRepository extends JpaRepository<BookingCohost, Long> {
    List<BookingCohost> findByBookingId(Long bookingId);
    Optional<BookingCohost> findByPayToken(String payToken);
}
