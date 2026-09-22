package com.smartspace.entry.repository;

import com.smartspace.entry.entity.HandoverReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HandoverReportRepository extends JpaRepository<HandoverReport, Long> {
    Optional<HandoverReport> findByBookingIdAndPhase(Long bookingId, HandoverReport.HandoverPhase phase);
}
