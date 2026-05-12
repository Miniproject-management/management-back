package com.mini3.backend.integration.documentanalyzer;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 기존 백엔드 URL 그대로 ATS HR API 를 document-analyzer 로 전달한다.
 */
@RestController
@RequestMapping("/api/hr/applicants")
@RequiredArgsConstructor
@ConditionalOnBean(DocumentAnalyzerProxyClient.class)
public class AtsHrApplicantsProxyController {

    private final DocumentAnalyzerProxyClient proxyClient;

    @GetMapping
    public ResponseEntity<byte[]> list(HttpServletRequest request) {
        return proxyClient.forward(request, null);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<byte[]> dashboard(HttpServletRequest request) {
        return proxyClient.forward(request, null);
    }

    @GetMapping("/{applicantId}/resume/preview-url")
    public ResponseEntity<byte[]> resumePreviewUrl(
            @PathVariable Long applicantId,
            HttpServletRequest request
    ) {
        return proxyClient.forward(request, null);
    }

    @GetMapping("/{applicantId}")
    public ResponseEntity<byte[]> detail(
            @PathVariable Long applicantId,
            HttpServletRequest request
    ) {
        return proxyClient.forward(request, null);
    }

    @PostMapping("/{applicantId}/analyze")
    public ResponseEntity<byte[]> analyze(
            @PathVariable Long applicantId,
            HttpServletRequest request
    ) throws IOException {
        byte[] body = request.getInputStream().readAllBytes();
        return proxyClient.forward(request, body.length == 0 ? null : body);
    }
}
