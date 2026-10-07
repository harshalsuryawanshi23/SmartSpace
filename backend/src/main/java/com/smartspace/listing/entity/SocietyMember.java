package com.smartspace.listing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "society_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(SocietyMemberId.class)
public class SocietyMember {

    @Id
    @Column(name = "society_id", columnDefinition = "BIGINT UNSIGNED")
    private Long societyId;

    @Id
    @Column(name = "user_id", columnDefinition = "BIGINT UNSIGNED")
    private Long userId;

    @Column(name = "flat_label", length = 30)
    private String flatLabel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('PENDING','APPROVED','REMOVED')")
    private SocietyMemberStatus status;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = SocietyMemberStatus.PENDING;
        }
    }
}
