package com.smartspace.booking.repository;

import com.smartspace.booking.entity.PricingSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PricingSuggestionRepository extends JpaRepository<PricingSuggestion, Long> {
    List<PricingSuggestion> findByHallIdAndStatus(Long hallId, String status);
}
