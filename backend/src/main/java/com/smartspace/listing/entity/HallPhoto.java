package com.smartspace.listing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "hall_photos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HallPhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hall_id", nullable = false)
    private Long hallId;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(length = 160)
    private String caption;

    @Column(name = "sort_order", nullable = false, columnDefinition = "SMALLINT")
    private Integer sortOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.sortOrder == null) this.sortOrder = 0;
    }
}
