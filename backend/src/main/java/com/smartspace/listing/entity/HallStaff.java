package com.smartspace.listing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "hall_staff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HallStaff {

    @EmbeddedId
    private HallStaffId id;

    @Enumerated(EnumType.STRING)
    @Column(name = "staff_role", nullable = false, columnDefinition = "ENUM('WATCHMAN','MANAGER')")
    private StaffRole staffRole;

    @Column(nullable = false)
    private Boolean active;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.staffRole == null) this.staffRole = StaffRole.WATCHMAN;
        if (this.active == null) this.active = true;
    }
}
