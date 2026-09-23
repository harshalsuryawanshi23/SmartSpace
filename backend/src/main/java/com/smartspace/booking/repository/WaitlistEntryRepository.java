package com.smartspace.booking.repository;

import com.smartspace.booking.entity.WaitlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface WaitlistEntryRepository extends JpaRepository<WaitlistEntry, Long> {
    List<WaitlistEntry> findByHallIdAndStatusAndStartTimeBetweenOrderByCreatedAtAsc(Long hallId, String status, ZonedDateTime start, ZonedDateTime end);
}
