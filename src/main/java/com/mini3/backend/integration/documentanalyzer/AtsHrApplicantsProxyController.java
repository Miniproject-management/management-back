package com.mini3.backend.integration.documentanalyzer;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

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
    private final ManagementResumePdfService managementResumePdfService;

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

    /**
     * S3 바이트 스트림은 {@link org.springframework.core.io.InputStreamResource} 로 넘기면
     * 일부 서블릿/버전에서 메시지 변환 단계에서 500 이 날 수 있어 {@link StreamingResponseBody} 로 직접 복사한다.
     */
    @GetMapping(
            value = "/{applicantId}/resume/file",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<StreamingResponseBody> resumeFile(
            @PathVariable Long applicantId,
            HttpServletRequest request
    ) {
        ResponseInputStream<GetObjectResponse> s3 =
                managementResumePdfService.openResumePdfStream(applicantId, request);
        GetObjectResponse meta = s3.response();
        Long contentLength = meta.contentLength();

        StreamingResponseBody body = outputStream -> {
            try (ResponseInputStream<GetObjectResponse> in = s3) {
                in.transferTo(outputStream);
            }
        };

        ResponseEntity.BodyBuilder b = ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"resume.pdf\"")
                .contentType(MediaType.APPLICATION_PDF);
        if (contentLength != null && contentLength > 0) {
            b.contentLength(contentLength);
        }
        return b.body(body);
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
