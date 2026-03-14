package com.sch.capstone.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Swagger(OpenAPI) 설정 클래스
@Configuration
public class SwaggerConfig {
//    // OpenAPI 문서 객체를 Bean으로 등록
//    @Bean
//    public OpenAPI openAPI() {
//        // API 문서에 JWT 인증 스키마 추가
//        return new OpenAPI()
//                .components(new Components()
//                        .addSecuritySchemes("bearerAuth", // 스키마 이름
//                                new SecurityScheme()
//                                        .type(SecurityScheme.Type.HTTP) // 인증방식(HTTP)
//                                        .scheme("bearer") // Bearer 토큰 방식
//                                        .bearerFormat("JWT") // JWT 형식 사용
//                        )
//                )
//                // 모든 API 요청에 기본적으로 "bearerAuth" 보안 요구사항 추가
//                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
//    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .servers(java.util.List.of(new io.swagger.v3.oas.models.servers.Server().url("/")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}