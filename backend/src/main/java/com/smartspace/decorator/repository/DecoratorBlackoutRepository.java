package com.smartspace.decorator.repository;

import com.smartspace.decorator.entity.DecoratorBlackout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DecoratorBlackoutRepository extends JpaRepository<DecoratorBlackout, Long> {
    
    @Query("SELECT b FROM DecoratorBlackout b WHERE b.decorator.id = :decoratorId AND b.endTime > :start AND b.startTime < :end")
    List<DecoratorBlackout> findOverlappingBlackouts(@Param("decoratorId") Long decoratorId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<DecoratorBlackout> findByDecoratorId(Long decoratorId);
}
