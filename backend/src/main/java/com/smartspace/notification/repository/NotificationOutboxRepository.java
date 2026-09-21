package com.smartspace.notification.repository;

import com.smartspace.notification.entity.NotificationOutbox;
import com.smartspace.notification.entity.OutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {

    @Query("SELECT o FROM NotificationOutbox o WHERE o.status = :status AND o.nextAttemptAt <= :now")
    List<NotificationOutbox> findPendingNotifications(OutboxStatus status, Instant now, Pageable pageable);

    Optional<NotificationOutbox> findByDedupeKeyAndChannel(String dedupeKey, com.smartspace.notification.entity.NotificationChannel channel);
}
