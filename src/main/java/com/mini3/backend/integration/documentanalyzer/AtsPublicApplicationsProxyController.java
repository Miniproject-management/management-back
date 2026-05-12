package com.mini3.backend.integration.documentanalyzer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * 기존 백엔드 URL 그대로 공개 지원 API 를 document-analyzer 로 전달한다.
 */
@RestController
@RequestMapping("/api/public/applications")
@RequiredArgsConstructor
@ConditionalOnBean(DocumentAnalyzerProxyClient.class)
public class AtsPublicApplicationsProxyController {

    private final DocumentAnalyzerProxyClient proxyClient;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> submit(MultipartHttpServletRequest request) {
        return proxyClient.forwardMultipart(request);
    }
}
