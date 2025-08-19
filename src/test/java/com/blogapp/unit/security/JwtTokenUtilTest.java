package com.blogapp.unit.security;

import com.blogapp.security.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * JwtTokenUtil 단위 테스트
 * 
 * JWT 토큰 생성, 검증, 파싱 기능을 테스트합니다.
 * 실제 JWT 라이브러리를 사용하여 토큰의 완전성을 검증합니다.
 * 
 * 테스트 범위:
 * - 토큰 생성 및 파싱
 * - 토큰 만료 검증
 * - 사용자 정보 추출
 * - Refresh Token 처리
 * - 보안 관련 예외 처리
 * 
 * 보안 고려사항:
 * - 테스트용 비밀키 사용
 * - 토큰 무결성 검증
 * - 만료 시간 검증
 * - 권한 정보 검증
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-01-01
 */
@ActiveProfiles("test")
@DisplayName("JwtTokenUtil 단위 테스트")
class JwtTokenUtilTest {

    private JwtTokenUtil jwtTokenUtil;
    private UserDetails testUserDetails;
    
    @BeforeEach
    void setUp() {
        // 테스트용 JwtTokenUtil 초기화
        jwtTokenUtil = new JwtTokenUtil();
        
        // 테스트용 사용자 정보
        testUserDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(List.of(
                    new SimpleGrantedAuthority("ROLE_USER"),
                    new SimpleGrantedAuthority("ROLE_CUSTOMER")
                ))
                .build();
    }
    
    @Test
    @DisplayName("Access Token 생성 및 검증")
    void generateToken_Success() {
        // When
        String token = jwtTokenUtil.generateToken(testUserDetails);
        
        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT는 3부분으로 구성
        
        // 토큰에서 사용자명 추출 검증
        String username = jwtTokenUtil.getUsernameFromToken(token);
        assertThat(username).isEqualTo("testuser");
    }
    
    @Test
    @DisplayName("Refresh Token 생성 및 검증")
    void generateRefreshToken_Success() {
        // When
        String refreshToken = jwtTokenUtil.generateRefreshToken(testUserDetails);
        
        // Then
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();
        assertThat(refreshToken.split("\\.")).hasSize(3);
        
        // Refresh Token에서 사용자명 추출 검증
        String username = jwtTokenUtil.getUsernameFromToken(refreshToken);
        assertThat(username).isEqualTo("testuser");
    }
    
    @Test
    @DisplayName("토큰에서 사용자명 추출")
    void getUsernameFromToken_Success() {
        // Given
        String token = jwtTokenUtil.generateToken(testUserDetails);
        
        // When
        String username = jwtTokenUtil.getUsernameFromToken(token);
        
        // Then
        assertThat(username).isEqualTo("testuser");
    }
    
    @Test
    @DisplayName("토큰에서 만료 시간 추출")
    void getExpirationDateFromToken_Success() {
        // Given
        String token = jwtTokenUtil.generateToken(testUserDetails);
        
        // When
        var expirationDate = jwtTokenUtil.getExpirationDateFromToken(token);
        
        // Then
        assertThat(expirationDate).isNotNull();
        assertThat(expirationDate).isAfter(new java.util.Date());
    }
    
    @Test
    @DisplayName("유효한 토큰 검증")
    void validateToken_ValidToken_ReturnsTrue() {
        // Given
        String token = jwtTokenUtil.generateToken(testUserDetails);
        
        // When
        boolean isValid = jwtTokenUtil.validateToken(token, testUserDetails);
        
        // Then
        assertThat(isValid).isTrue();
    }
    
    @Test
    @DisplayName("잘못된 사용자 정보로 토큰 검증")
    void validateToken_WrongUser_ReturnsFalse() {
        // Given
        String token = jwtTokenUtil.generateToken(testUserDetails);
        
        UserDetails wrongUser = User.builder()
                .username("wronguser")
                .password("password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
        
        // When
        boolean isValid = jwtTokenUtil.validateToken(token, wrongUser);
        
        // Then
        assertThat(isValid).isFalse();
    }
    
    @Test
    @DisplayName("Refresh Token 유효성 검증")
    void validateRefreshToken_ValidToken_ReturnsTrue() {
        // Given
        String refreshToken = jwtTokenUtil.generateRefreshToken(testUserDetails);
        
        // When
        boolean isValid = jwtTokenUtil.validateRefreshToken(refreshToken);
        
        // Then
        assertThat(isValid).isTrue();
    }
    
    @Test
    @DisplayName("토큰 만료 여부 확인")
    void isTokenExpired_ValidToken_ReturnsFalse() {
        // Given
        String token = jwtTokenUtil.generateToken(testUserDetails);
        
        // When
        boolean isExpired = jwtTokenUtil.isTokenExpired(token);
        
        // Then
        assertThat(isExpired).isFalse();
    }
    
    @Test
    @DisplayName("토큰에서 권한 정보 추출")
    void getAuthoritiesFromToken_Success() {
        // Given
        String token = jwtTokenUtil.generateToken(testUserDetails);
        
        // When
        var authorities = jwtTokenUtil.getAuthoritiesFromToken(token);
        
        // Then
        assertThat(authorities).isNotNull();
        assertThat(authorities).hasSize(2);
        assertThat(authorities).contains("ROLE_USER", "ROLE_CUSTOMER");
    }
    
    @Test
    @DisplayName("잘못된 토큰으로 사용자명 추출 시도")
    void getUsernameFromToken_InvalidToken_ThrowsException() {
        // Given
        String invalidToken = "invalid.jwt.token";
        
        // When & Then
        assertThatThrownBy(() -> jwtTokenUtil.getUsernameFromToken(invalidToken))
                .isInstanceOf(RuntimeException.class);
    }
    
    @Test
    @DisplayName("null 토큰 검증")
    void validateToken_NullToken_ReturnsFalse() {
        // When
        boolean isValid = jwtTokenUtil.validateToken(null, testUserDetails);
        
        // Then
        assertThat(isValid).isFalse();
    }
    
    @Test
    @DisplayName("빈 토큰 검증")
    void validateToken_EmptyToken_ReturnsFalse() {
        // When
        boolean isValid = jwtTokenUtil.validateToken("", testUserDetails);
        
        // Then
        assertThat(isValid).isFalse();
    }
    
    @Test
    @DisplayName("토큰 생성 시 사용자 정보 포함 확인")
    void generateToken_ContainsUserInfo() {
        // Given & When
        String token = jwtTokenUtil.generateToken(testUserDetails);
        
        // Then - 토큰에서 모든 정보를 정확히 추출할 수 있어야 함
        String extractedUsername = jwtTokenUtil.getUsernameFromToken(token);
        var extractedAuthorities = jwtTokenUtil.getAuthoritiesFromToken(token);
        var expirationDate = jwtTokenUtil.getExpirationDateFromToken(token);
        
        assertThat(extractedUsername).isEqualTo(testUserDetails.getUsername());
        assertThat(extractedAuthorities)
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_CUSTOMER");
        assertThat(expirationDate).isAfter(new java.util.Date());
    }
}