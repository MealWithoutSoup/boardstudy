package com.blogapp.config;

import com.blogapp.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 보안 설정 클래스
 * 
 * 블로그 시스템의 전체 보안 구성을 정의하는 설정 클래스입니다.
 * JWT 기반 인증, 역할 기반 접근 제어, CORS 설정 등을 포함합니다.
 * 
 * 주요 보안 기능:
 * - JWT 토큰 기반 stateless 인증
 * - 역할 기반 접근 제어 (ADMIN, USER)
 * - BCrypt 비밀번호 암호화
 * - CORS 설정
 * - CSRF 보호 비활성화 (API 서버용)
 * - 세션 관리 정책
 * 
 * 설계 원칙:
 * - 최소 권한 원칙 적용
 * - 명시적 보안 규칙 정의
 * - 확장 가능한 구조
 * - 개발/운영 환경 고려
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-01-01
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    /**
     * JWT 인증 필터
     * 
     * 모든 요청에서 JWT 토큰을 검증하고 인증 컨텍스트를 설정하는 필터입니다.
     * UsernamePasswordAuthenticationFilter 이전에 실행됩니다.
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 사용자 상세 정보 서비스
     * 
     * 사용자 인증 시 사용자 정보를 로드하는 서비스입니다.
     * CustomUserDetailsService 구현체를 주입받아 사용합니다.
     */
    private final UserDetailsService userDetailsService;

    /**
     * 비밀번호 암호화 빈 등록
     * 
     * BCrypt 알고리즘을 사용하여 비밀번호를 암호화하는 PasswordEncoder를 제공합니다.
     * 
     * BCrypt 특징:
     * - 솔트 자동 생성 및 적용
     * - 설정 가능한 강도 (기본값: 10)
     * - 레인보우 테이블 공격 방지
     * - 단방향 해시 함수
     * 
     * 사용 예시:
     * - 회원가입 시 비밀번호 암호화
     * - 로그인 시 비밀번호 검증
     * 
     * @return BCrypt 기반 PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("BCrypt PasswordEncoder 빈 등록");
        return new BCryptPasswordEncoder();
    }

    /**
     * 인증 제공자 빈 등록
     * 
     * 사용자명/비밀번호 기반 인증을 처리하는 DaoAuthenticationProvider를 설정합니다.
     * 
     * 구성 요소:
     * - UserDetailsService: 사용자 정보 로드
     * - PasswordEncoder: 비밀번호 검증
     * 
     * @return 설정된 DaoAuthenticationProvider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        
        log.info("DaoAuthenticationProvider 빈 등록 완료");
        return authProvider;
    }

    /**
     * 인증 매니저 빈 등록
     * 
     * Spring Security의 인증 처리를 담당하는 AuthenticationManager를 제공합니다.
     * 로그인 API에서 사용자 인증 시 사용됩니다.
     * 
     * @param config 인증 설정
     * @return AuthenticationManager 인스턴스
     * @throws Exception 설정 오류 시
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        log.info("AuthenticationManager 빈 등록");
        return config.getAuthenticationManager();
    }

    /**
     * CORS 설정 빈 등록
     * 
     * Cross-Origin Resource Sharing 설정을 정의합니다.
     * 프론트엔드 애플리케이션에서 API 호출을 허용하기 위해 필요합니다.
     * 
     * 허용 설정:
     * - Origins: localhost:3000, localhost:8080 (개발 환경)
     * - Methods: GET, POST, PUT, DELETE, OPTIONS
     * - Headers: 모든 헤더 허용
     * - Credentials: 인증 정보 포함 요청 허용
     * 
     * 프로덕션 환경에서는 구체적인 도메인으로 제한해야 합니다.
     * 
     * @return CORS 설정 소스
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 허용할 Origin 설정 (프로덕션에서는 구체적인 도메인으로 변경)
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000",  // React 개발 서버
            "http://localhost:8080",  // Spring Boot 개발 서버
            "https://yourdomain.com"  // 프로덕션 도메인
        ));
        
        // 허용할 HTTP 메서드 설정
        configuration.setAllowedMethods(Arrays.asList(
            HttpMethod.GET.name(),
            HttpMethod.POST.name(),
            HttpMethod.PUT.name(),
            HttpMethod.DELETE.name(),
            HttpMethod.OPTIONS.name()
        ));
        
        // 허용할 헤더 설정
        configuration.setAllowedHeaders(List.of("*"));
        
        // 인증 정보 포함 요청 허용
        configuration.setAllowCredentials(true);
        
        // 브라우저 캐시 시간 설정 (초)
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        log.info("CORS 설정 완료");
        return source;
    }

    /**
     * Security Filter Chain 설정
     * 
     * Spring Security의 핵심 보안 설정을 정의합니다.
     * 
     * 주요 설정:
     * - CSRF 비활성화 (API 서버용)
     * - CORS 활성화
     * - 세션 관리 정책 (STATELESS)
     * - 접근 권한 규칙
     * - JWT 인증 필터 추가
     * 
     * @param http HttpSecurity 설정 객체
     * @return 설정된 SecurityFilterChain
     * @throws Exception 설정 오류 시
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Security Filter Chain 설정 시작");
        
        http
            // CSRF 보호 비활성화 (JWT 사용 시 불필요)
            .csrf(AbstractHttpConfigurer::disable)
            
            // CORS 설정 활성화
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 세션 관리 정책: STATELESS (JWT 사용으로 세션 불필요)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 인증 제공자 설정
            .authenticationProvider(authenticationProvider())
            
            // URL별 접근 권한 설정
            .authorizeHttpRequests(authz -> authz
                // === 공개 접근 허용 경로 ===
                
                // 인증 관련 API (로그인, 회원가입)
                .requestMatchers(
                    AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/auth/login"),
                    AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/auth/register"),
                    AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/auth/refresh")
                ).permitAll()
                
                // 공개 API (발행된 게시글 조회 등)
                .requestMatchers(
                    AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/public/**")
                ).permitAll()
                
                // 헬스 체크 엔드포인트
                .requestMatchers(
                    AntPathRequestMatcher.antMatcher("/actuator/health")
                ).permitAll()
                
                // === 인증 필요 경로 ===
                
                // 사용자 프로필 관리
                .requestMatchers(
                    AntPathRequestMatcher.antMatcher("/api/users/profile/**")
                ).authenticated()
                
                // 게시글 작성/수정/삭제
                .requestMatchers(
                    AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/posts/**"),
                    AntPathRequestMatcher.antMatcher(HttpMethod.PUT, "/api/posts/**"),
                    AntPathRequestMatcher.antMatcher(HttpMethod.DELETE, "/api/posts/**")
                ).authenticated()
                
                // 파일 업로드
                .requestMatchers(
                    AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/files/**")
                ).authenticated()
                
                // === 관리자 전용 경로 ===
                
                // 사용자 관리
                .requestMatchers(
                    AntPathRequestMatcher.antMatcher("/api/admin/users/**")
                ).hasAuthority("ADMIN")
                
                // 시스템 관리
                .requestMatchers(
                    AntPathRequestMatcher.antMatcher("/api/admin/system/**")
                ).hasAuthority("ADMIN")
                
                // 모든 관리자 API
                .requestMatchers(
                    AntPathRequestMatcher.antMatcher("/api/admin/**")
                ).hasAuthority("ADMIN")
                
                // === 기타 모든 요청 ===
                // 명시되지 않은 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )
            
            // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 이전에 추가
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        log.info("Security Filter Chain 설정 완료");
        return http.build();
    }
}