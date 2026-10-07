package com.smartspace.decorator.repository;

import com.smartspace.decorator.entity.DecoratorEnquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DecoratorEnquiryRepository extends JpaRepository<DecoratorEnquiry, Long> {
    List<DecoratorEnquiry> findByBookingId(Long bookingId);
    List<DecoratorEnquiry> findByDecoratorIdOrderByCreatedAtDesc(Long decoratorId);
    
    @Query("SELECT e FROM DecoratorEnquiry e WHERE e.decorator.id = :decoratorId AND e.status IN ('ACCEPTED', 'CONFIRMED_BY_RENTER') AND e.booking.startAt < :end AND e.booking.endAt > :start")
    List<DecoratorEnquiry> findOverlappingEnquiries(@Param("decoratorId") Long decoratorId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<DecoratorEnquiry> findByStatusAndCreatedAtBefore(DecoratorEnquiry.EnquiryStatus status, LocalDateTime dateTime);
}
