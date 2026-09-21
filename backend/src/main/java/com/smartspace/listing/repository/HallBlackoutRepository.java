package com.smartspace.listing.repository;

import com.smartspace.listing.entity.HallBlackout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HallBlackoutRepository extends JpaRepository<HallBlackout, Long> {
    List<HallBlackout> findByHallId(Long hallId);
    List<HallBlackout> findByHallIdAndEndAtAfter(Long hallId, LocalDateTime date);
}
