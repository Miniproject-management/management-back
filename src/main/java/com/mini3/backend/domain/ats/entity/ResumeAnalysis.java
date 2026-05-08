package com.mini3.backend.domain.ats.entity;

import com.mini3.backend.domain.ats.enums.AnalysisStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 이력서 분석 엔티티
@Entity
@Table(name = "resume_analyses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_id")
    private Long analysisId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AnalysisStatus status;

    @Column(name = "model", length = 100)
    private String model;

    @Lob
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Lob
    @Column(name = "result_json", columnDefinition = "TEXT")
    private String resultJson;

    @Column(name = "failure_message", length = 2000)
    private String failureMessage;

    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt;

    @PrePersist
    void prePersist() {
        if (this.status == null) {
            this.status = AnalysisStatus.PENDING;
        }
    }
}
