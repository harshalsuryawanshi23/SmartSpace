package com.smartspace.dispute.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "dispute_evidence")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisputeEvidence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispute_id", nullable = false)
    private Dispute dispute;

    @Column(name = "submitted_by", nullable = false)
    private Long submittedBy;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "sha256", length = 64, columnDefinition = "CHAR(64)")
    private String sha256;

    @Column(name = "note", length = 1000)
    private String note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
