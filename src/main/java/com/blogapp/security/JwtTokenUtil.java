package com.blogapp.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT 토큰 유틸리티 클래스
 * 
 * JWT(JSON Web Token) 토큰의 생성, 검증, 파싱을 담당하는 유틸리티 클래스입니다.
 * 
 * 주요 기능:
 * - JWT 토큰 생성 (Access Token, Refresh Token)
 * - 토큰 유효성 검증
 * - 토큰에서 클레임 정보 추출
 * - 토큰 만료 시간 관리
 * - 보안키 관리
 * 
 * 보안 특징:
 * - HMAC SHA-256 알고리즘 사용
 * - 환경 변수를 통한 시크릿 키 관리
 * - 토큰 만료 시간 설정 (Access: 1시간, Refresh: 7일)
 * - 예외 처리 및 로깅
 * 
 * 사용 예시:
 * String token = jwtTokenUtil.generateToken(userDetails);
 * String username = jwtTokenUtil.getUsernameFromToken(token);
 * boolean isValid = jwtTokenUtil.validateToken(token, userDetails);
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-01-01
 */
@Component
@Slf4j
public class JwtTokenUtil {

    /**
     * JWT 시크릿 키
     * 
     * JWT 토큰의 서명 생성 및 검증에 사용되는 비밀키입니다.
     * application.yml 파일이나 환경 변수에서 설정됩니다.
     * 
     * 보안 요구사항:
     * - 최소 32자 이상의 복잡한 문자열
     * - 프로덕션 환경에서는 환경 변수로 관리
     * - 정기적으로 로테이션 권장
     */
    @Value("${jwt.secret:mySecretKey123456789012345678901234567890}")
    private String secret;

    /**
     * Access Token 만료 시간 (초 단위)
     * 
     * Access Token의 유효 기간을 초 단위로 설정합니다.
     * 기본값: 3600초 (1시간)
     * 
     * 보안 고려사항:
     * - 너무 길면 보안 위험 증가
     * - 너무 짧으면 사용자 경험 저하
     * - 일반적으로 15분~1시간 권장
     */
    @Value("${jwt.expiration:3600}")
    private Long jwtExpiration;

    /**
     * Refresh Token 만료 시간 (초 단위)
     * 
     * Refresh Token의 유효 기간을 초 단위로 설정합니다.
     * 기본값: 604800초 (7일)
     * 
     * 보안 고려사항:
     * - Access Token보다 길게 설정
     * - 일반적으로 7일~30일 권장
     * - 보안 정책에 따라 조정 가능
     */
    @Value("${jwt.refresh.expiration:604800}")
    private Long refreshExpiration;

    /**
     * JWT 서명 키 생성
     * 
     * 시크릿 문자열로부터 HMAC SHA-256 서명 키를 생성합니다.
     * 
     * @return 서명에 사용할 SecretKey 객체
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 토큰에서 사용자명 추출
     * 
     * JWT 토큰의 subject 클레임에서 사용자명을 추출합니다.
     * 
     * @param token JWT 토큰
     * @return 사용자명
     * @throws JwtException 토큰이 유효하지 않은 경우
     */
    public String getUsernameFromToken(String token) {
        log.debug("토큰에서 사용자명 추출 시도");
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 토큰에서 만료 시간 추출
     * 
     * JWT 토큰의 exp 클레임에서 만료 시간을 추출합니다.
     * 
     * @param token JWT 토큰
     * @return 만료 시간
     * @throws JwtException 토큰이 유효하지 않은 경우
     */
    public Date getExpirationDateFromToken(String token) {
        log.debug("토큰에서 만료 시간 추출 시도");
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 토큰에서 특정 클레임 추출
     * 
     * JWT 토큰에서 지정된 클레임을 추출하는 범용 메서드입니다.
     * 
     * @param token JWT 토큰
     * @param claimsResolver 클레임 추출 함수
     * @param <T> 반환할 클레임의 타입
     * @return 추출된 클레임 값
     * @throws JwtException 토큰이 유효하지 않은 경우
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = getAllClaimsFromToken(token);
            return claimsResolver.apply(claims);
        } catch (JwtException e) {
            log.error("토큰에서 클레임 추출 실패: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 토큰에서 모든 클레임 추출
     * 
     * JWT 토큰을 파싱하여 모든 클레임 정보를 추출합니다.
     * 
     * @param token JWT 토큰
     * @return 모든 클레임이 포함된 Claims 객체
     * @throws JwtException 토큰이 유효하지 않은 경우
     */
    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰: {}", e.getMessage());
            throw new JwtException("토큰이 만료되었습니다.", e);
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰: {}", e.getMessage());
            throw new JwtException("지원되지 않는 토큰입니다.", e);
        } catch (MalformedJwtException e) {
            log.error("잘못된 형식의 JWT 토큰: {}", e.getMessage());
            throw new JwtException("토큰 형식이 올바르지 않습니다.", e);
        } catch (SecurityException | IllegalArgumentException e) {
            log.error("JWT 토큰 검증 실패: {}", e.getMessage());
            throw new JwtException("토큰 검증에 실패했습니다.", e);
        }
    }

    /**
     * 토큰 만료 여부 확인
     * 
     * JWT 토큰이 만료되었는지 확인합니다.
     * 
     * @param token JWT 토큰
     * @return 만료되었으면 true, 아니면 false
     */
    public Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            boolean expired = expiration.before(new Date());
            log.debug("토큰 만료 확인 - 만료 여부: {}", expired);
            return expired;
        } catch (JwtException e) {
            log.warn("토큰 만료 확인 중 오류 발생: {}", e.getMessage());
            return true; // 오류 발생 시 만료된 것으로 처리
        }
    }

    /**
     * UserDetails로부터 Access Token 생성
     * 
     * 사용자 정보를 바탕으로 Access Token을 생성합니다.
     * 
     * @param userDetails Spring Security UserDetails 객체
     * @return 생성된 JWT Access Token
     */
    public String generateToken(UserDetails userDetails) {
        log.info("사용자 '{}'에 대한 Access Token 생성 시작", userDetails.getUsername());
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername(), jwtExpiration);
    }

    /**
     * UserDetails로부터 Refresh Token 생성
     * 
     * 사용자 정보를 바탕으로 Refresh Token을 생성합니다.
     * 
     * @param userDetails Spring Security UserDetails 객체
     * @return 생성된 JWT Refresh Token
     */
    public String generateRefreshToken(UserDetails userDetails) {
        log.info("사용자 '{}'에 대한 Refresh Token 생성 시작", userDetails.getUsername());
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "refresh");
        return createToken(claims, userDetails.getUsername(), refreshExpiration);
    }

    /**
     * 클레임과 사용자명으로 토큰 생성
     * 
     * 지정된 클레임과 사용자명, 만료 시간을 사용하여 JWT 토큰을 생성합니다.
     * 
     * @param claims 토큰에 포함할 추가 클레임
     * @param subject 토큰의 주체 (사용자명)
     * @param expiration 만료 시간 (초 단위)
     * @return 생성된 JWT 토큰
     */
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + expiration * 1000);
            
            String token = Jwts.builder()
                    .claims(claims)
                    .subject(subject)
                    .issuedAt(now)
                    .expiration(expiryDate)
                    .signWith(getSigningKey())
                    .compact();
            
            log.info("토큰 생성 완료 - 사용자: {}, 만료시간: {}", subject, expiryDate);
            return token;
        } catch (Exception e) {
            log.error("토큰 생성 실패 - 사용자: {}, 오류: {}", subject, e.getMessage());
            throw new RuntimeException("토큰 생성에 실패했습니다.", e);
        }
    }

    /**
     * 토큰 유효성 검증
     * 
     * JWT 토큰이 유효한지 검증합니다.
     * 사용자명 일치 여부와 만료 여부를 확인합니다.
     * 
     * @param token JWT 토큰
     * @param userDetails 검증할 사용자 정보
     * @return 유효하면 true, 아니면 false
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);
            
            log.info("토큰 검증 결과 - 사용자: {}, 유효성: {}", username, isValid);
            return isValid;
        } catch (JwtException e) {
            log.warn("토큰 검증 실패 - 오류: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Refresh Token 유효성 검증
     * 
     * Refresh Token이 유효한지 검증합니다.
     * 토큰 타입과 만료 여부를 확인합니다.
     * 
     * @param token Refresh Token
     * @return 유효하면 true, 아니면 false
     */
    public Boolean validateRefreshToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            String tokenType = (String) claims.get("tokenType");
            boolean isRefreshToken = "refresh".equals(tokenType);
            boolean isNotExpired = !isTokenExpired(token);
            
            boolean isValid = isRefreshToken && isNotExpired;
            log.info("Refresh Token 검증 결과 - 유효성: {}", isValid);
            return isValid;
        } catch (JwtException e) {
            log.warn("Refresh Token 검증 실패 - 오류: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰 남은 만료 시간 계산 (초 단위)
     * 
     * JWT 토큰의 남은 유효 시간을 초 단위로 계산합니다.
     * 
     * @param token JWT 토큰
     * @return 남은 만료 시간 (초), 만료된 경우 0
     */
    public Long getTokenRemainingTime(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            Date now = new Date();
            long remaining = (expiration.getTime() - now.getTime()) / 1000;
            return Math.max(0, remaining);
        } catch (JwtException e) {
            log.warn("토큰 남은 시간 계산 실패: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * 토큰에서 권한 정보 추출
     * 
     * JWT 토큰에서 사용자의 권한 정보를 추출합니다.
     * (추후 권한 정보를 토큰에 포함할 경우 사용)
     * 
     * @param token JWT 토큰
     * @return 권한 정보 문자열
     */
    public String getAuthoritiesFromToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return (String) claims.get("authorities");
        } catch (JwtException e) {
            log.warn("토큰에서 권한 정보 추출 실패: {}", e.getMessage());
            return null;
        }
    }
}