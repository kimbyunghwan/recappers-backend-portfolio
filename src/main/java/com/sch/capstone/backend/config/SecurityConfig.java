package com.sch.capstone.backend.config;

import com.sch.capstone.backend.filter.JwtAuthenticationFilter;
import com.sch.capstone.backend.jwt.JwtUtil;
import com.sch.capstone.backend.service.security.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    // 사용자 정보를 DB에서 조회하는 클래스
    private final CustomUserDetailsService userDetailsService;
    // JWT 발급 및 검증 클래스
    private final JwtUtil jwtUtil;

    // CORS 허용 도메인 Origin 값 불러서 주입
    @Value("${app.cors.allow-origins}")
    private String allowedOrigins;

    /**
     * Spring Security의 필터 체인 설정
     * CSRF 비활성화
     * 세션을 사용하지 않는 Stateless 정책
     * JWT 필터 추가
     * CORS 정책 적용
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보호 비활성화 (JWT 기반 인증이라 CSRF 불 필요)
                .csrf(csrf -> csrf.disable())
                // 폼 로그인 방식 비활성화
                .formLogin(form -> form.disable())
                // HTTP Basic 인증 비활성화(JWT 인증 사용하여 불 필요)
                .httpBasic(basic -> basic.disable())
                // 세션을 아예 만들지 않는 Stateless 정책 (JWT 기반 인증)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //

                // 인증, 인가 실패 시 응답 정의
                .exceptionHandling(ex -> ex
                        // 인증 실패
                        .authenticationEntryPoint((req, res, e) -> {
                            // "WWW-Authenticate: Bearer" 헤더 추가
                            res.setHeader("WWW-Authenticate", "Bearer");
                            // 401 Unauthorized 응답
                            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                        // 인가 실패(권한부족) 403 에러
                        .accessDeniedHandler((req, res, e) -> {
                            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
                        })
                )

                // 요청 url 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 모든 URL(/**)에 대해서 사전 요청 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 콜백 허용
                        .requestMatchers(HttpMethod.GET,  "/ping").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/uploads/*/ocr/callback").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/nlp/callback/**").permitAll()
                        // 인증, 회원가입, 에러, swagger, OpenAPI 문서 엔드포인트는 누구나 접근 가능
                        .requestMatchers("/api/auth/**", "/error","/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // 역할별 보호 구간 추가
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/prof/**").hasRole("PROFESSOR")
                        .requestMatchers("/api/student/**").hasRole("STUDENT")

                        .requestMatchers(HttpMethod.GET, "/api/uploads/*/ocr").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/uploads/*/ocr/pages/*/image").permitAll()
                        
                        // 그 외 나머지 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // authenticationProvider()가 DB에 있는 사용자 이메일, 비밀번호랑 검증
                .authenticationProvider(authenticationProvider())
                // JWT 필터 연결
                // UsernamePasswordAuthenticationFilter가 실행되기 전에 JwtAuthenticationFilter 먼저 실행
                // JWT 필터를 로그인 처리 필터 앞에 둬서 토큰 검증이 이뤄지게 한다.(.formLogin()는 사용안하므로)
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)

                //CORS 정책
                .cors(cors -> cors.configurationSource(req -> {
                    CorsConfiguration c = new CorsConfiguration();

                    c.setAllowedOriginPatterns(List.of(
                            "http://localhost:*",
                            "http://127.0.0.1:*",
                            "http://192.168.0.5:*",
                            "http://220.69.208.121:*",
                            "https://*.ngrok-free.dev"
                    ));

                    // 허용 HTTP 메서드
                    c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS","HEAD"));
                    // 요청 헤더 모두 허용
                    c.setAllowedHeaders(List.of("*"));
                    // 쿠키, Authorization 헤더를 포함한 자격 증명 요청 허용
                    c.setAllowCredentials(true);
                    return c;
                }));

        // 위 체인 보안 규칙들을 적용해 SecurityFilterChain 빈으로 빌드해서 반환
        return http.build();
    }

    // Spring Security 필터 체인 자체를 아예 타지 않게 제외
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
                "/v3/api-docs",
                "/v3/api-docs/**",
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/swagger-resources/**",
                "/webjars/**",
                "/favicon.ico"
        );
    }

    // 로그인 할 때 사용자 인증 방법 정의
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService); // DB에서 사용자 조회
        provider.setPasswordEncoder(passwordEncoder()); // 비밀번호 검증(BCrypt)
        return provider;
    }

    // AuthenticationManager 빈에 등록
    // 로그인 시 authenticationManager.authenticate(UsernamePasswordAuthenticationToken) 호출에 사용되어
    // UserDetailsService와 PasswordEncoder를 통해 아이디/비밀번호를 검증한다.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // PasswordEncoder 타입의 빈을 등록(BCrypt 알고리즘을 사용하여 비밀번호를 단방향 해시 처리)
    // UserService 클래스에 주입받아 회원가입/로그인 시 비밀번호 암호화·검증에 사용됨
    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}
