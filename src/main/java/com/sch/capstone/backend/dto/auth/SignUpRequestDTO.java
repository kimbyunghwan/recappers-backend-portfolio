package com.sch.capstone.backend.dto.auth;

import com.sch.capstone.backend.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

// 회원가입 요청 (name, password, email)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원가입 요청")
public class SignUpRequestDTO {

    @NotBlank
    @Schema(description = "사용자 이름")
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, message = "비밀번호는 최소 8자리 이상이어야 합니다.")
    @Schema(description = "비밀번호 (최소 8자리)")
    private String password;

    @NotNull(message = "role 입력 필수, (STUDENT/PROFESSOR/ADMIN)")
    @Schema(description = "역할 (STUDENT, PROFESSOR, ADMIN)")
    private Role role;
}
