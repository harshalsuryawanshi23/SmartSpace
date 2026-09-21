package com.smartspace.listing.repository;

import com.smartspace.listing.entity.HallOpeningHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HallOpeningHoursRepository extends JpaRepository<HallOpeningHours, Long> {
    List<HallOpeningHours> findByHallId(Long hallId);
    void deleteByHallId(Long hallId);
}
