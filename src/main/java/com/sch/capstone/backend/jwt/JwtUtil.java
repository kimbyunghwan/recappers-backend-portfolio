package com.sch.capstone.backend.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private final Key signingKey; // JWT 서명에 사용할 HMAC 키
    private final long accessExpMs; // 엑세스 토큰 유효기간(밀리초 단위)

    public JwtUtil(
            @Value("${jwt.secret}") String secret, // application.yml 설정에서 가져온 비밀 키
            @Value("${jwt.access-exp-ms}") long accessExpMs // 엑세스 토큰 만료 시간
    ) {
        // HS256 서명용 키 생성
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessExpMs = accessExpMs;
    }

    /** 액세스 토큰 생성 (subject = email) */
    public String generateAccessToken(String email, String role) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", role);

        Date now = new Date();
        Date exp = new Date(now.getTime() + accessExpMs); // 만료 시간 = 현재 + 설정 값

        return Jwts.builder()
                .setClaims(extraClaims) // 사용자 정의 클레임
                .setSubject(email) // 토큰의 사용자 식별자
                .setIssuedAt(now) // 발급 시간
                .setExpiration(exp) // 만료 시간
                .signWith(signingKey, SignatureAlgorithm.HS256) // HS256 알고리즘으로 서명
                .compact(); // 문자열 형태의 JWT 생성
    }

    // 토큰에서 이메일 추출
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    // 토큰에서 만료 시각 추출
    public long getExpiration(String token) {
        return extractClaim(token, Claims::getExpiration).getTime();
    }

    // 토큰의 유효성 검증(서명이 올바른지, 형식이 맞는지, 만료되지 않았는지 검사)
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder().
                    setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 공통 클레임 추출(원하는 클레임을 resolver로 꺼냄)
    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey) // 검증용 키
                .build()
                .parseClaimsJws(token)// 토큰파싱 및 검증
                .getBody(); // payload 반환
        return resolver.apply(claims); // 원하는 값 추출
    }
}
