package com.mini3.backend.integration.documentanalyzer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.Enumeration;
import java.util.List;

/**
 * 이력서 PDF는 document-analyzer 의 /resume/file 프록시에서 403 이 나는 환경이 있어,
 * 이미 동작하는 상세 API로 storageKey 만 받은 뒤 백엔드(IRSA)에서 S3 GetObject 로 직접 제공한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(DocumentAnalyzerProxyClient.class)
public class ManagementResumePdfService {

    private final RestTemplate restTemplate;
    private final IntegrationDocumentAnalyzerProperties properties;
    private final ObjectMapper objectMapper;
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public byte[] loadPdfBytes(long applicantId, HttpServletRequest incomingRequest) {
        String base = properties.getBaseUrl().replaceAll("/$", "");
        String detailUrl = base + "/api/hr/applicants/" + applicantId;

        HttpHeaders headers = copyForwardableHeaders(incomingRequest);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        try {
            ResponseEntity<byte[]> detailRes = restTemplate.exchange(
                    detailUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    byte[].class
            );
            if (!detailRes.getStatusCode().is2xxSuccessful() || detailRes.getBody() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "지원자 상세 응답이 올바르지 않습니다: HTTP " + detailRes.getStatusCode()
                );
            }
            JsonNode root = objectMapper.readTree(detailRes.getBody());
            String key = root.path("storageKey").asText(null);
            if (key == null || key.isBlank()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "제출된 이력서(S3 키)가 없습니다.");
            }
            return fetchFromS3(key.strip());
        } catch (ResponseStatusException e) {
            throw e;
        } catch (HttpStatusCodeException e) {
            log.warn(
                    "document-analyzer 상세 호출 실패 applicantId={} status={}",
                    applicantId,
                    e.getStatusCode(),
                    e
            );
            throw new ResponseStatusException(
                    HttpStatus.valueOf(e.getStatusCode().value()),
                    "지원자 상세 조회에 실패했습니다.",
                    e
            );
        } catch (Exception e) {
            log.error("이력서 PDF 로드 실패 applicantId={}", applicantId, e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "이력서 PDF를 불러오지 못했습니다.",
                    e
            );
        }
    }

    private byte[] fetchFromS3(String objectKey) {
        GetObjectRequest req = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();
        try {
            return s3Client.getObjectAsBytes(req).asByteArray();
        } catch (S3Exception e) {
            log.warn("S3 GetObject 거부/실패 bucket={} key={} status={}", bucket, objectKey, e.statusCode(), e);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "S3에서 파일을 읽지 못했습니다. 백엔드 IAM에 s3:GetObject 권한이 있는지 확인하세요.",
                    e
            );
        } catch (SdkServiceException e) {
            log.error("S3 SDK 오류 bucket={} key={}", bucket, objectKey, e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "S3 연결 오류가 발생했습니다.", e);
        }
    }

    private static HttpHeaders copyForwardableHeaders(HttpServletRequest request) {
        HttpHeaders out = new HttpHeaders();
        Enumeration<String> names = request.getHeaderNames();
        if (names == null) {
            return out;
        }
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (name == null) {
                continue;
            }
            String lower = name.toLowerCase();
            if ("host".equals(lower)
                    || "connection".equals(lower)
                    || "content-length".equals(lower)
                    || "content-type".equals(lower)) {
                continue;
            }
            Enumeration<String> values = request.getHeaders(name);
            while (values != null && values.hasMoreElements()) {
                out.add(name, values.nextElement());
            }
        }
        return out;
    }
}
