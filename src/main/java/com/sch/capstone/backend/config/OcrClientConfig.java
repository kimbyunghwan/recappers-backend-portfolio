package com.sch.capstone.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OcrClientConfig {

    @Bean("ocrClient")
    public WebClient ocrClient(@Value("${ocr.base-url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .exchangeStrategies(builder -> builder
                        .codecs(c -> c.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)) // ★ 16MB
                )
                .build();
    }
}
