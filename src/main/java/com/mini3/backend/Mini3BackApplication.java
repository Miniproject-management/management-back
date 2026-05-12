package com.mini3.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 운영에서 Repository 스캔 중복 등록 이슈를 피하기 위해
 * JPA Repository/Entity 스캔 경로를 명시적으로 1회만 설정한다.
 */
@SpringBootApplication
public class Mini3BackApplication {

    public static void main(String[] args) {
        SpringApplication.run(Mini3BackApplication.class, args);
    }
}
