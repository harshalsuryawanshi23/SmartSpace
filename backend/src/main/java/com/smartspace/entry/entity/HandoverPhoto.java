package com.smartspace.entry.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "handover_photos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HandoverPhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private HandoverReport report;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "sha256", nullable = false, length = 64, columnDefinition = "CHAR(64)")
    private String sha256;

    @Column(name = "taken_at", nullable = false)
    private LocalDateTime takenAt;

    @Column(name = "caption", length = 160)
    private String caption;
}
