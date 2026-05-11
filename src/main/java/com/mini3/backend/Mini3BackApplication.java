package com.mini3.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
    scanBasePackages = {
        "com.mini3.backend"
    }
)
public class Mini3BackApplication {

    public static void main(String[] args) {
        SpringApplication.run(Mini3BackApplication.class, args);
    }
}
