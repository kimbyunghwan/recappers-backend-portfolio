package com.sch.capstone.backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

// 로그인 요청(email, password) 클래스
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "로그인 요청")
public class AuthRequestDTO {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
