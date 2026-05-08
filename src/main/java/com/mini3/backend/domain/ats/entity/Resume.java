package com.mini3.backend.domain.ats.entity;

import com.mini3.backend.domain.ats.enums.ResumeSource;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 이력서 엔티티
@Entity
@Table(name = "resumes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume {

    @Id // PK 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resume_id") // 이력서 ID
    private Long resumeId;

    @ManyToOne(fetch = FetchType.LAZY) // 여러 개의 Resume가 하나의 Applicant 에 속함.
    @JoinColumn(name = "applicant_id", nullable = false) // Applicant ID
    private Applicant applicant;

    @Column(name = "title", length = 200) // 이력서 제목
    private String title;

    @Lob // Large Object
    @Column(name = "content", nullable = false, columnDefinition = "TEXT") // 이력서 내용
    private String content;

    @Enumerated(EnumType.STRING) // DB에는 숫자가 아니라 문자열로
    @Column(name = "source", nullable = false, length = 20)
    private ResumeSource source;

    @Column(name = "original_file_name", length = 255)
    private String originalFileName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
