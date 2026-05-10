package com.mini3.backend.domain.ats.service;

import com.mini3.backend.domain.ats.dto.AtsAnalysisDto;
import com.mini3.backend.domain.ats.dto.AtsApplicantDto;
import com.mini3.backend.domain.ats.dto.AtsSubmitDto;
import com.mini3.backend.domain.ats.entity.Applicant;
import com.mini3.backend.domain.ats.entity.Resume;
import com.mini3.backend.domain.ats.entity.ResumeAnalysis;
import com.mini3.backend.domain.ats.enums.AnalysisStatus;
import com.mini3.backend.domain.ats.enums.ResumeSource;
import com.mini3.backend.domain.ats.repository.ApplicantRepository;
import com.mini3.backend.domain.ats.repository.ResumeAnalysisRepository;
import com.mini3.backend.domain.ats.repository.ResumeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AtsService {

    private final ApplicantRepository applicantRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;

    /**
     * 공개 지원: 인적사항 + 파일 1건 저장. S3 업로드는 추후 연동 시 storageKey 등 확장.
     */
    @Transactional
    public AtsSubmitDto.SubmitResponse submitApplication(AtsSubmitDto.SubmitRequest request, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("첨부 파일이 필요합니다.");
        }

        Applicant applicant = Applicant.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();
        applicantRepository.save(applicant);

        String content = resolveUploadedText(file);
        Resume resume = Resume.builder()
                .applicant(applicant)
                .title(null)
                .content(content)
                .source(ResumeSource.UPLOAD)
                .originalFileName(file.getOriginalFilename())
                .build();
        resumeRepository.save(resume);

        return AtsSubmitDto.SubmitResponse.builder()
                .applicantId(applicant.getApplicantId())
                .resumeId(resume.getResumeId())
                .message("제출이 완료되었습니다.")
                .build();
    }

    public List<AtsApplicantDto.ListItem> getApplicants() {
        return applicantRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(AtsApplicantDto.ListItem::from)
                .toList();
    }

    public AtsApplicantDto.Detail getApplicantDetail(Long applicantId) {
        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new EntityNotFoundException("지원자를 찾을 수 없습니다."));

        AtsApplicantDto.Detail.DetailBuilder builder = AtsApplicantDto.Detail.builder()
                .applicantId(applicant.getApplicantId())
                .name(applicant.getName())
                .email(applicant.getEmail())
                .phone(applicant.getPhone())
                .submittedAt(applicant.getCreatedAt());

        resumeRepository.findFirstByApplicant_ApplicantIdOrderByCreatedAtDesc(applicantId)
                .ifPresent(resume -> {
                    builder.resumeId(resume.getResumeId())
                            .resumeTitle(resume.getTitle())
                            .source(resume.getSource() != null ? resume.getSource().name() : null)
                            .originalFileName(resume.getOriginalFileName())
                            .storageKey(null);

                    resumeAnalysisRepository.findFirstByResume_ResumeIdOrderByAnalyzedAtDesc(resume.getResumeId())
                            .ifPresent(analysis -> builder.analysis(AtsAnalysisDto.Analysis.from(analysis)));
                });

        return builder.build();
    }

    /**
     * 최근 제출 이력서 기준 AI 분석. Gemini 연동 전에는 스텁 결과를 저장한다.
     */
    @Transactional
    public AtsAnalysisDto.AnalyzeResult analyzeApplicantResume(Long applicantId) {
        applicantRepository.findById(applicantId)
                .orElseThrow(() -> new EntityNotFoundException("지원자를 찾을 수 없습니다."));
        Resume resume = resumeRepository.findFirstByApplicant_ApplicantIdOrderByCreatedAtDesc(applicantId)
                .orElseThrow(() -> new EntityNotFoundException("제출된 이력서가 없습니다."));

        ResumeAnalysis analysis = ResumeAnalysis.builder()
                .resume(resume)
                .status(AnalysisStatus.COMPLETED)
                .model("stub")
                .summary("Gemini API 연동 후 자동 분석됩니다.")
                .resultJson("{\"decision\":\"REVIEW\",\"overallScore\":0}")
                .analyzedAt(LocalDateTime.now())
                .build();
        resumeAnalysisRepository.save(analysis);

        return AtsAnalysisDto.AnalyzeResult.builder()
                .message("분석이 완료되었습니다.")
                .analysis(AtsAnalysisDto.Analysis.from(analysis))
                .build();
    }

    private static String resolveUploadedText(MultipartFile file) {
        String original = file.getOriginalFilename();
        if (original != null && original.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            try {
                try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    String text = stripper.getText(doc);
                    if (text != null && !text.isBlank()) {
                        return text;
                    }
                }
            } catch (IOException ignored) {
                // fall through to placeholder
            }
        }
        return "[업로드 파일] " + (original != null ? original : "첨부");
    }
}
