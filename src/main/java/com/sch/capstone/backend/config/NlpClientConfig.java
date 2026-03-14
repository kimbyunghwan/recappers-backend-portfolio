package com.sch.capstone.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

// NLP 서버와 통신하기 위한 WebClient 설정 클래스
@Configuration
public class NlpClientConfig {
    @Value("${nlp.base-url}")
    String baseUrl;//NLP 서버 주소

    @Value("${nlp.timeout-ms:15000}")
    int timeoutMs;

    // NLP API 호출 전용 WebClient Bean 등록
    @Bean
    public WebClient nlpClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-Client", "capstone-backend") // 간단한 클라이언트 식별용
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .responseTimeout(java.time.Duration.ofMillis(timeoutMs))
                ))
                .filter(ExchangeFilterFunctions.statusError(
                        HttpStatusCode::isError,
                        res -> (new RuntimeException("NLP error " + res.statusCode()))
                ))
                .filter((req, next) -> {
                    System.out.println("➡ NLP " + req.method() + " " + req.url());
                    return next.exchange(req)
                            .doOnNext(res -> System.out.println("⬅ NLP " + res.statusCode()));
                })
                .build();
    }
}

