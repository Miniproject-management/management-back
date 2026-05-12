package com.mini3.backend.integration.documentanalyzer;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(IntegrationDocumentAnalyzerProperties.class)
public class DocumentAnalyzerProxyConfig {

    @Bean
    RestTemplate documentAnalyzerRestTemplate() {
        return new RestTemplate();
    }
}
