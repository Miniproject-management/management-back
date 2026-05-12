package com.mini3.backend.integration.documentanalyzer;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ATS API 는 document-analyzer 로 분리됐을 때, 기존 Postman/프론트 URL(백엔드)을 유지하기 위한 리버스 프록시 설정.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "integration.document-analyzer")
public class IntegrationDocumentAnalyzerProperties {

    /** false 이면 프록시 비활성(해당 경로는 404). */
    private boolean enabled = true;

    /** 예: 클러스터 내부 {@code http://document-analyzer-service:8080} */
    private String baseUrl = "http://document-analyzer-service:8080";
}
