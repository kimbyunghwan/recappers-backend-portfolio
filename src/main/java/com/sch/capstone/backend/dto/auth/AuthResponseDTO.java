package com.sch.capstone.backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "로그인 응답")
public class AuthResponseDTO {
    private Long id;
    private String name;
    private String email;

    private String role;
    
    @Schema(description = "JWT 액세스 토큰", example = "eyJh...")
    private String token;
    @Schema(description = "토큰 만료 시각", example = "1754...")
    private long expiresAt;
}