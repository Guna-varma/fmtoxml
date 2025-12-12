package com.cms.projects.transformation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "com.cms.projects.transformation")
@EnableAsync
public class TransformationApplication {
    public static void main(String[] args) {
        SpringApplication.run(TransformationApplication.class, args);
    }
}

