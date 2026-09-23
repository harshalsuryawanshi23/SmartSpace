package com.smartspace.user.entity;

import com.smartspace.core.entity.BaseEntity;
import com.smartspace.listing.entity.Hall;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "society_members")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class SocietyMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @Column(nullable = false)
    private String status; // PENDING, APPROVED, REJECTED
}
