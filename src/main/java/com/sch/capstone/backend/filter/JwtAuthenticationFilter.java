package com.sch.capstone.backend.filter;

import lombok.extern.slf4j.Slf4j;
import com.sch.capstone.backend.jwt.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    // 필터 동작(요청마다 실행)
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        String method = request.getMethod();
        String authHeader = request.getHeader("Authorization");
        log.debug("[JWT] {} {} | Authorization={}", method, uri, authHeader);

        // 이미 인증돼 있으면 통과
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 요청에서 토큰 추출(헤더, 쿠키 확인)
        String token = resolveToken(request);

        // 토큰이 존재하고 유효한 경우
        if (token != null && jwtUtil.isTokenValid(token)) {
            try{
                String email = jwtUtil.extractEmail(token); // 토큰에서 사용자 이메일 추출

                // 스프링 시큐리티 인증 객체 생성(권한은 빈 리스트)
                var auth = new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());

                // SecurityContext에 인증 정보 저장 이후 컨트롤러에서 인증 사용자로 인식
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ignore) {

            }
        }
        // 다음 필터, 컨트롤러로 요청 넘김
        filterChain.doFilter(request, response);
    }

    // 필터 적용하지 않는 요청 경로 지정(Swagger UI, API 문서, 회원가입/로그인, CORS 프리플라이트 등은 JWT 검사 제외)
    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String uri = req.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) return true; // CORS 프리플라이트
        return uri.startsWith("/v3/api-docs")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/swagger-resources")
                || uri.startsWith("/webjars")
                || uri.startsWith("/api/auth");
    }

    // 요청에서 JWT 토큰을 추출하는 메서드
    private String resolveToken(HttpServletRequest req) {
        // 1) Authorization: Bearer xxx
        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) return h.substring(7);

        // 2) HttpOnly 쿠키 Access_Token
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("Access_Token".equals(c.getName())) return c.getValue();
            }
        }
        return null;
    }
}
