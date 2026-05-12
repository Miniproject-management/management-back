package com.mini3.backend.integration.documentanalyzer;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import java.util.Collections;
import java.util.Enumeration;

@Slf4j
@Component
@RequiredArgsConstructor
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
        prefix = "integration.document-analyzer",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class DocumentAnalyzerProxyClient {

    private final IntegrationDocumentAnalyzerProperties properties;
    private final RestTemplate restTemplate;

    public ResponseEntity<byte[]> forward(HttpServletRequest request, byte[] bodyOrNull) {
        String base = properties.getBaseUrl().replaceAll("/$", "");
        String url = base + request.getRequestURI();
        if (request.getQueryString() != null) {
            url += "?" + request.getQueryString();
        }

        HttpHeaders out = copyRequestHeaders(request);
        HttpEntity<byte[]> entity = bodyOrNull == null
                ? new HttpEntity<>(out)
                : new HttpEntity<>(bodyOrNull, out);
        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        try {
            return restTemplate.exchange(url, method, entity, byte[].class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .headers(filterResponseHeaders(e.getResponseHeaders()))
                    .body(e.getResponseBodyAsByteArray());
        } catch (Exception e) {
            log.error("document-analyzer 프록시 실패 url={}", url, e);
            throw e;
        }
    }

    public ResponseEntity<byte[]> forwardMultipart(MultipartHttpServletRequest request) {
        String base = properties.getBaseUrl().replaceAll("/$", "");
        String url = base + request.getRequestURI();
        String query = request.getQueryString();
        if (query != null) {
            url += "?" + query;
        }

        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        for (String name : request.getMultiFileMap().keySet()) {
            for (MultipartFile file : request.getFiles(name)) {
                if (!file.isEmpty()) {
                    parts.add(name, file.getResource());
                }
            }
        }
        for (String param : request.getParameterMap().keySet()) {
            if (request.getFileMap().containsKey(param)) {
                continue;
            }
            for (String value : request.getParameterValues(param)) {
                parts.add(param, value);
            }
        }

        HttpHeaders out = copyRequestHeaders(request);
        out.remove(HttpHeaders.CONTENT_TYPE);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(parts, out);
        try {
            return restTemplate.postForEntity(url, entity, byte[].class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .headers(filterResponseHeaders(e.getResponseHeaders()))
                    .body(e.getResponseBodyAsByteArray());
        }
    }

    private static HttpHeaders copyRequestHeaders(HttpServletRequest request) {
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
            if ("host".equals(lower) || "connection".equals(lower) || "content-length".equals(lower)) {
                continue;
            }
            Enumeration<String> values = request.getHeaders(name);
            while (values != null && values.hasMoreElements()) {
                out.add(name, values.nextElement());
            }
        }
        return out;
    }

    private static HttpHeaders filterResponseHeaders(HttpHeaders in) {
        if (in == null) {
            return new HttpHeaders();
        }
        HttpHeaders out = new HttpHeaders();
        for (String key : in.keySet()) {
            if (key == null) {
                continue;
            }
            String lower = key.toLowerCase();
            if ("transfer-encoding".equals(lower)) {
                continue;
            }
            out.put(key, in.getOrDefault(key, Collections.emptyList()));
        }
        return out;
    }
}
