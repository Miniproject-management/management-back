package com.mini3.backend.domain.ats.controller;

import com.mini3.backend.domain.ats.dto.AtsAnalysisDto;
import com.mini3.backend.domain.ats.dto.AtsApplicantDto;
import com.mini3.backend.domain.ats.service.AtsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hr/applicants")
@RequiredArgsConstructor
public class AtsHrController {

    private final AtsService atsService;

    @GetMapping
    public ResponseEntity<List<AtsApplicantDto.ListItem>> getApplicants() {
        return ResponseEntity.ok(atsService.getApplicants());
    }

    @GetMapping("/{applicantId}")
    public ResponseEntity<AtsApplicantDto.Detail> getApplicantDetail(@PathVariable Long applicantId) {
        return ResponseEntity.ok(atsService.getApplicantDetail(applicantId));
    }

    @PostMapping("/{applicantId}/analyze")
    public ResponseEntity<AtsAnalysisDto.AnalyzeResult> analyzeApplicant(@PathVariable Long applicantId) {
        return ResponseEntity.ok(atsService.analyzeApplicantResume(applicantId));
    }
}
