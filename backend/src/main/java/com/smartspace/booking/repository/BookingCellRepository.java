package com.smartspace.booking.repository;

import com.smartspace.booking.entity.BookingCell;
import com.smartspace.booking.entity.BookingCellId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface BookingCellRepository extends JpaRepository<BookingCell, BookingCellId> {

    List<BookingCell> findByIdHallIdAndIdCellStartBetween(Long hallId, Instant start, Instant end);

    @Modifying
    @Query("DELETE FROM BookingCell c WHERE c.bookingId = :bookingId")
    void deleteByBookingId(@Param("bookingId") Long bookingId);
}
