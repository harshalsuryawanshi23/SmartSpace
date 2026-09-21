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
public class SocietyMember {

    @EmbeddedId
    private SocietyMemberId id;

    @Column(name = "flat_label", length = 30)
    private String flatLabel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
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
