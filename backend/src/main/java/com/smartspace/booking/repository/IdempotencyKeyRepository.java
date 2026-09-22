package com.smartspace.booking.repository;

import com.smartspace.booking.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, String> {
    Optional<IdempotencyKey> findByIdempotencyKeyAndUserId(String idempotencyKey, Long userId);

    @Modifying
    @Query("DELETE FROM IdempotencyKey k WHERE k.createdAt < :cutoff")
    void deleteOlderThan(@Param("cutoff") Instant cutoff);
}
