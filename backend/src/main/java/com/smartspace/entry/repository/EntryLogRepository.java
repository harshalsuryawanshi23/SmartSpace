package com.smartspace.entry.repository;

import com.smartspace.entry.entity.EntryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntryLogRepository extends JpaRepository<EntryLog, Long> {
    boolean existsByClientEventId(String clientEventId);
}
