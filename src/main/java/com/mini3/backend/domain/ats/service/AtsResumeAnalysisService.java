package com.mini3.backend.domain.ats.service;

import com.mini3.backend.domain.ats.dto.AtsAnalysisDto;
import com.mini3.backend.domain.ats.entity.Resume;
import com.mini3.backend.domain.ats.entity.ResumeAnalysis;
import com.mini3.backend.domain.ats.enums.AnalysisStatus;
import com.mini3.backend.domain.ats.repository.ApplicantRepository;
import com.mini3.backend.domain.ats.repository.ResumeAnalysisRepository;
import com.mini3.backend.domain.ats.repository.ResumeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 이력서 AI 분석: S3 키로 연결된 {@link Resume}에 대해 {@link ResumeAnalysis}를 생성·저장한다.
 * Google GenAI 연동 시 {@link com.mini3.backend.global.storage.AtsS3StorageService#getObjectBytes(String)} 로
 * 원본을 읽어 모델에 넘기면 된다.
 */
@Service
@RequiredArgsConstructor
public class AtsResumeAnalysisService {

    private final ApplicantRepository applicantRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;

    /**
     * 지원자의 최신 제출 이력서를 분석한다. 현재는 스텁이며 {@code overallScore}·{@code resultJson} 형식만 맞춘다.
     */
    @Transactional
    public AtsAnalysisDto.AnalyzeResult analyzeApplicantResume(Long applicantId) {
        applicantRepository.findById(applicantId)
                .orElseThrow(() -> new EntityNotFoundException("지원자를 찾을 수 없습니다."));
        Resume resume = resumeRepository.findFirstByApplicant_ApplicantIdOrderByCreatedAtDesc(applicantId)
                .orElseThrow(() -> new EntityNotFoundException("제출된 이력서가 없습니다."));

        if (resume.getS3ObjectKey() == null || resume.getS3ObjectKey().isBlank()) {
            throw new IllegalStateException("S3에 저장된 이력서 키가 없어 분석할 수 없습니다.");
        }

        int stubScore = 0;
        ResumeAnalysis analysis = ResumeAnalysis.builder()
                .resume(resume)
                .status(AnalysisStatus.COMPLETED)
                .model("stub")
                .summary("Google GenAI 연동 후 자동 분석됩니다. 현재는 S3 파일 존재 확인 후 스텁 점수입니다.")
                .resultJson("{\"decision\":\"REVIEW\",\"overallScore\":" + stubScore + "}")
                .overallScore(stubScore)
                .analyzedAt(LocalDateTime.now())
                .build();
        resumeAnalysisRepository.save(analysis);

        return AtsAnalysisDto.AnalyzeResult.builder()
                .message("분석이 완료되었습니다.")
                .analysis(AtsAnalysisDto.Analysis.from(analysis))
                .build();
    }
}
