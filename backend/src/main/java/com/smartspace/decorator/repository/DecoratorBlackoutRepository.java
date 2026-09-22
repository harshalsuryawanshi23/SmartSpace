package com.smartspace.decorator.repository;

import com.smartspace.decorator.entity.DecoratorBlackout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DecoratorBlackoutRepository extends JpaRepository<DecoratorBlackout, Long> {
    List<DecoratorBlackout> findByDecoratorIdAndStartAtBeforeAndEndAtAfter(Long decoratorId, LocalDateTime end, LocalDateTime start);
}
