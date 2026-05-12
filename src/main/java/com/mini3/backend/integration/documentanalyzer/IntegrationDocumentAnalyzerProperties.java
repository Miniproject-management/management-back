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

    /** 클러스터 내부 서비스 DNS (짧은 이름 대신 FQDN이 안정적) */
    private String baseUrl =
            "http://document-analyzer-service.miniproject-management.svc.cluster.local:8080";

    /** document-analyzer 연결 타임아웃(ms) */
    private int connectTimeoutMs = 10_000;

    /** 분석(Gemini) 등 장시간 응답 허용(ms). ALB idle timeout(기본 60s)보다 길면 ALB에서 먼저 끊길 수 있음 */
    private int readTimeoutMs = 180_000;
}
