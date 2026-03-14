package com.sch.capstone.backend.controller;

import com.sch.capstone.backend.dto.auth.AuthRequestDTO;
import com.sch.capstone.backend.dto.auth.AuthResponseDTO;
import com.sch.capstone.backend.dto.auth.SignUpRequestDTO;
import com.sch.capstone.backend.dto.user.UserDTO;
import com.sch.capstone.backend.entity.User;
import com.sch.capstone.backend.jwt.JwtUtil;
import com.sch.capstone.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

// 인증/인가 관련 컨트롤러
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<UserDTO> signUp(@Valid @RequestBody SignUpRequestDTO req) {
        User saved = userService.signUp(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UserDTO(saved.getId(), saved.getName(), saved.getEmail(), saved.getRole()));
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO req,
                                         HttpServletResponse res) {
        // 스프링 시큐리티 인증(비번 검증)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        User user = userService.findByEmail(req.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // 액세스 토큰 발급 → HttpOnly 쿠키로 내려줌
        String token = jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());

        ResponseCookie cookie = ResponseCookie.from("Access_Token", token)
                .httpOnly(true) // JS로 접근 불가
                .secure(true) // HTTPS에서만 쿠키 전송하도록 제한
                .sameSite("None") // 서로 다른 도메인에서 요청할 때도 쿠키 전송 허용
                .path("/") // 사이트 전역 경로에 쿠키 전송
                .build();
        res.addHeader("Set-Cookie", cookie.toString()); // 응답 헤더에 Set-Cookie 추가

        User u = userService.findByEmail(req.getEmail()).orElseThrow(); // 이메일로 사용자 조회

        return ResponseEntity.ok(AuthResponseDTO.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .role(user.getRole().name())
                .token(token) // body 토큰 출력
                .expiresAt(jwtUtil.getExpiration(token))
                .build());
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse res) {
        // 기존 브라우저에 저장된 JWT 토큰을 지우기 위해 동일 이름으로 빈 쿠키 생성
        Cookie cookie = new Cookie("Access_Token", "");
        // JS 접근 불가
        cookie.setHttpOnly(true);
        // 쿠키 적용 범위를 전체 경로로 지정
        cookie.setPath("/");
        // 만료 시간 0초 -> 즉시 삭제 가능하도록 한다.
        cookie.setMaxAge(0);
        // 빈 값(cookie)을 넣어 쿠기 삭제
        res.addCookie(cookie);
        // 로그아웃 성공 시 204 No Contnent 응답 반환
        return ResponseEntity.noContent().build();
    }
}
