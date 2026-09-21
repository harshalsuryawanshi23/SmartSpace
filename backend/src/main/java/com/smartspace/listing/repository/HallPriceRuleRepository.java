package com.smartspace.listing.repository;

import com.smartspace.listing.entity.HallPriceRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HallPriceRuleRepository extends JpaRepository<HallPriceRule, Long> {
    List<HallPriceRule> findByHallIdOrderByPriorityDesc(Long hallId);
    void deleteByHallId(Long hallId);
}
