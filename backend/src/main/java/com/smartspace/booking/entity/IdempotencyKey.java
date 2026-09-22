package com.smartspace.booking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "idempotency_keys")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class IdempotencyKey {

    @Id
    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "request_path", nullable = false, length = 200)
    private String requestPath;

    @Column(name = "locked_at", nullable = false)
    private Instant lockedAt;

    @Column(name = "response_status")
    private Integer responseStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_body", columnDefinition = "json")
    private String responseBody;

    @Column(name = "completed_at")
    private Instant completedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
