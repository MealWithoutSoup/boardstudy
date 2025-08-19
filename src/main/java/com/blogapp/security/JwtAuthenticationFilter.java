package com.blogapp.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 * 
 * 모든 HTTP 요청에서 JWT 토큰을 검증하고 Spring Security 인증 컨텍스트를 설정하는 필터입니다.
 * OncePerRequestFilter를 상속하여 요청당 한 번만 실행되도록 보장합니다.
 * 
 * 주요 기능:
 * - HTTP 헤더에서 JWT 토큰 추출
 * - JWT 토큰 유효성 검증
 * - 유효한 토큰인 경우 SecurityContext에 인증 정보 설정
 * - 인증 실패 시 적절한 로깅 및 오류 처리
 * 
 * 필터 체인 순서:
 * 1. 요청 헤더에서 토큰 추출
 * 2. 토큰 유효성 검증
 * 3. 사용자 정보 로드
 * 4. 인증 객체 생성 및 SecurityContext 설정
 * 5. 다음 필터로 전달
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-01-01
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * JWT 토큰 유틸리티
     * 
     * JWT 토큰의 생성, 검증, 파싱을 담당하는 유틸리티 클래스입니다.
     * 토큰에서 사용자명 추출, 유효성 검증 등에 사용됩니다.
     */
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * 사용자 상세 정보 서비스
     * 
     * 사용자명으로부터 UserDetails 객체를 로드하는 서비스입니다.
     * JWT 토큰에서 추출한 사용자명으로 실제 사용자 정보를 조회할 때 사용됩니다.
     */
    private final UserDetailsService userDetailsService;

    /**
     * Authorization 헤더의 Bearer 토큰 접두사
     * 
     * HTTP Authorization 헤더에서 JWT 토큰을 식별하기 위한 접두사입니다.
     * 표준 형식: "Bearer {JWT_TOKEN}"
     */
    private static final String TOKEN_PREFIX = "Bearer ";

    /**
     * Authorization 헤더명
     * 
     * JWT 토큰이 포함된 HTTP 헤더의 이름입니다.
     */
    private static final String HEADER_STRING = "Authorization";

    /**
     * 필터 실행 메서드
     * 
     * 모든 HTTP 요청에 대해 JWT 인증을 수행하는 핵심 메서드입니다.
     * 
     * 처리 과정:
     * 1. Authorization 헤더에서 JWT 토큰 추출
     * 2. 토큰 유효성 검증 및 사용자명 추출
     * 3. 현재 SecurityContext에 인증 정보가 없는 경우에만 처리
     * 4. UserDetailsService를 통해 사용자 정보 로드
     * 5. 토큰과 사용자 정보 검증
     * 6. 인증 객체 생성 및 SecurityContext 설정
     * 7. 다음 필터로 요청 전달
     * 
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param filterChain 필터 체인
     * @throws ServletException 서블릿 예외
     * @throws IOException I/O 예외
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 1. 요청에서 JWT 토큰 추출
            String jwt = getJwtFromRequest(request);
            
            // 2. 토큰이 존재하고 유효한 경우에만 처리
            if (StringUtils.hasText(jwt)) {
                processJwtAuthentication(jwt, request);
            } else {
                log.debug("JWT 토큰이 요청에 포함되지 않음 - URI: {}", request.getRequestURI());
            }
            
        } catch (JwtException e) {
            // JWT 관련 예외 처리
            log.warn("JWT 토큰 처리 중 오류 발생 - URI: {}, 오류: {}", 
                    request.getRequestURI(), e.getMessage());
            // 인증 실패 시 SecurityContext를 비우고 계속 진행
            SecurityContextHolder.clearContext();
            
        } catch (Exception e) {
            // 기타 예외 처리
            log.error("인증 필터에서 예상치 못한 오류 발생 - URI: {}, 오류: {}", 
                     request.getRequestURI(), e.getMessage(), e);
            SecurityContextHolder.clearContext();
        }
        
        // 3. 다음 필터로 요청 전달 (인증 성공/실패와 관계없이)
        filterChain.doFilter(request, response);
    }

    /**
     * 요청에서 JWT 토큰 추출
     * 
     * HTTP 요청의 Authorization 헤더에서 JWT 토큰을 추출합니다.
     * 
     * 추출 과정:
     * 1. Authorization 헤더 값 확인
     * 2. "Bearer " 접두사 확인
     * 3. 접두사 제거 후 실제 토큰 부분만 반환
     * 
     * 예시:
     * - 헤더 값: "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     * - 반환 값: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     * 
     * @param request HTTP 요청 객체
     * @return 추출된 JWT 토큰, 없으면 null
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(HEADER_STRING);
        
        // Authorization 헤더가 존재하고 "Bearer "로 시작하는지 확인
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            String token = bearerToken.substring(TOKEN_PREFIX.length());
            log.debug("요청에서 JWT 토큰 추출 완료 - URI: {}", request.getRequestURI());
            return token;
        }
        
        return null;
    }

    /**
     * JWT 인증 처리
     * 
     * 추출된 JWT 토큰을 검증하고 Spring Security 인증 컨텍스트를 설정합니다.
     * 
     * 처리 과정:
     * 1. JWT 토큰에서 사용자명 추출
     * 2. 현재 SecurityContext에 인증 정보가 없는지 확인
     * 3. UserDetailsService를 통해 사용자 정보 로드
     * 4. JWT 토큰과 사용자 정보 검증
     * 5. 인증 객체 생성 및 SecurityContext 설정
     * 
     * @param jwt JWT 토큰
     * @param request HTTP 요청 객체 (인증 세부 정보 설정용)
     * @throws JwtException JWT 토큰 관련 오류
     */
    private void processJwtAuthentication(String jwt, HttpServletRequest request) {
        try {
            // 1. JWT 토큰에서 사용자명 추출
            String username = jwtTokenUtil.getUsernameFromToken(jwt);
            log.debug("JWT 토큰에서 사용자명 추출: {}", username);
            
            // 2. 사용자명이 존재하고 현재 인증되지 않은 상태인지 확인
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // 3. 사용자 정보 로드
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                log.debug("사용자 정보 로드 완료: {}", username);
                
                // 4. JWT 토큰 유효성 검증
                if (jwtTokenUtil.validateToken(jwt, userDetails)) {
                    
                    // 5. 인증 객체 생성
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails, 
                            null, 
                            userDetails.getAuthorities()
                        );
                    
                    // 6. 인증 세부 정보 설정 (IP 주소, 세션 ID 등)
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 7. SecurityContext에 인증 정보 설정
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.info("JWT 인증 성공 - 사용자: {}, IP: {}", 
                            username, request.getRemoteAddr());
                } else {
                    log.warn("JWT 토큰 검증 실패 - 사용자: {}", username);
                }
            }
            
        } catch (Exception e) {
            log.error("JWT 인증 처리 중 오류 발생: {}", e.getMessage());
            throw new JwtException("JWT 인증 처리 실패", e);
        }
    }

    /**
     * 필터 적용 제외 경로 판단
     * 
     * 특정 요청 경로에 대해 이 필터를 적용하지 않을지 결정합니다.
     * 
     * 현재 구현:
     * - 모든 요청에 대해 필터 적용 (false 반환)
     * - 추후 공개 API 경로 등에 대해 필터 제외 로직 추가 가능
     * 
     * 제외 가능한 경로 예시:
     * - /api/auth/login
     * - /api/auth/register
     * - /api/public/**
     * - /actuator/health
     * 
     * @param request HTTP 요청 객체
     * @return 필터를 건너뛸지 여부 (true: 건너뛰기, false: 필터 적용)
     * @throws ServletException 서블릿 예외
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // 공개 API 경로 확인 (추후 확장 가능)
        if (path.startsWith("/api/auth/") || 
            path.startsWith("/api/public/") ||
            path.equals("/actuator/health")) {
            log.debug("JWT 필터 제외 경로: {}", path);
            return true;
        }
        
        return false;
    }
}