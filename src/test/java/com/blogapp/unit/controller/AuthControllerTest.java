package com.blogapp.unit.controller;

import com.blogapp.controller.AuthController;
import com.blogapp.dto.request.LoginRequest;
import com.blogapp.dto.request.UserRegistrationRequest;
import com.blogapp.dto.response.JwtAuthenticationResponse;
import com.blogapp.dto.response.UserResponse;
import com.blogapp.security.JwtTokenUtil;
import com.blogapp.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 단위 테스트
 * 
 * 인증 관련 REST API 컨트롤러의 단위 테스트입니다.
 * MockMvc를 사용하여 웹 레이어만 테스트하고 하위 계층은 모킹합니다.
 * 
 * 테스트 범위:
 * - HTTP 요청/응답 처리
 * - JSON 직렬화/역직렬화
 * - 입력값 검증 (Bean Validation)
 * - 에러 핸들링 및 상태 코드
 * - 보안 설정 (일부)
 * 
 * Mock 대상:
 * - UserService: 비즈니스 로직 계층
 * - AuthenticationManager: Spring Security 인증
 * - JwtTokenUtil: JWT 토큰 처리
 * - UserDetailsService: 사용자 정보 로드
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-01-01
 */
@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
@DisplayName("AuthController 단위 테스트")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private AuthenticationManager authenticationManager;
    
    @MockBean
    private JwtTokenUtil jwtTokenUtil;
    
    @MockBean
    private UserDetailsService userDetailsService;
    
    @MockBean
    private UserService userService;
    
    private LoginRequest validLoginRequest;
    private UserRegistrationRequest validRegistrationRequest;
    private UserResponse mockUserResponse;
    private UserDetails mockUserDetails;
    
    @BeforeEach
    void setUp() {
        setupTestData();
    }
    
    /**
     * 테스트 데이터 초기화
     */
    private void setupTestData() {
        // 로그인 요청 데이터
        validLoginRequest = new LoginRequest();
        validLoginRequest.setUsername("testuser");
        validLoginRequest.setPassword("password123");
        
        // 회원가입 요청 데이터
        validRegistrationRequest = new UserRegistrationRequest();
        validRegistrationRequest.setUsername("newuser");
        validRegistrationRequest.setEmail("newuser@example.com");
        validRegistrationRequest.setPassword("password123");
        validRegistrationRequest.setFirstName("New");
        validRegistrationRequest.setLastName("User");
        
        // Mock 사용자 응답
        mockUserResponse = new UserResponse();
        mockUserResponse.setId(1L);
        mockUserResponse.setUsername("testuser");
        mockUserResponse.setEmail("test@example.com");
        mockUserResponse.setFirstName("Test");
        mockUserResponse.setLastName("User");
        mockUserResponse.setIsActive(true);
        mockUserResponse.setRoles(List.of("USER"));
        mockUserResponse.setCreatedAt(LocalDateTime.now());
        
        // Mock UserDetails
        mockUserDetails = org.springframework.security.core.userdetails.User.builder()
                .username("testuser")
                .password("encoded_password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }
    
    @Test
    @DisplayName("로그인 성공")
    void login_Success() throws Exception {
        // Given
        Authentication mockAuthentication = new UsernamePasswordAuthenticationToken(
                mockUserDetails, null, mockUserDetails.getAuthorities());
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);
        when(jwtTokenUtil.generateToken(any(UserDetails.class)))
                .thenReturn("mock.access.token");
        when(jwtTokenUtil.generateRefreshToken(any(UserDetails.class)))
                .thenReturn("mock.refresh.token");
        
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock.access.token"))
                .andExpect(jsonPath("$.refreshToken").value("mock.refresh.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("testuser"));
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenUtil).generateToken(any(UserDetails.class));
        verify(jwtTokenUtil).generateRefreshToken(any(UserDetails.class));
    }
    
    @Test
    @DisplayName("로그인 실패 - 잘못된 인증 정보")
    void login_BadCredentials() throws Exception {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("잘못된 사용자명 또는 비밀번호입니다."));
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenUtil, never()).generateToken(any());
    }
    
    @Test
    @DisplayName("로그인 실패 - 입력값 검증 오류")
    void login_ValidationError() throws Exception {
        // Given - 빈 사용자명
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setUsername("");
        invalidRequest.setPassword("password123");
        
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        
        verify(authenticationManager, never()).authenticate(any());
    }
    
    @Test
    @DisplayName("회원가입 성공")
    void register_Success() throws Exception {
        // Given
        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenReturn(mockUserResponse);
        
        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.roles[0]").value("USER"));
        
        verify(userService).registerUser(any(UserRegistrationRequest.class));
    }
    
    @Test
    @DisplayName("회원가입 실패 - 입력값 검증 오류")
    void register_ValidationError() throws Exception {
        // Given - 잘못된 이메일 형식
        UserRegistrationRequest invalidRequest = new UserRegistrationRequest();
        invalidRequest.setUsername("newuser");
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setPassword("password123");
        invalidRequest.setFirstName("New");
        invalidRequest.setLastName("User");
        
        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        
        verify(userService, never()).registerUser(any());
    }
    
    @Test
    @DisplayName("토큰 갱신 성공")
    void refreshToken_Success() throws Exception {
        // Given
        String refreshToken = "Bearer mock.refresh.token";
        
        when(jwtTokenUtil.validateRefreshToken(anyString())).thenReturn(true);
        when(jwtTokenUtil.getUsernameFromToken(anyString())).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(mockUserDetails);
        when(jwtTokenUtil.generateToken(any(UserDetails.class))).thenReturn("new.access.token");
        
        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                .header("Authorization", refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new.access.token"))
                .andExpect(jsonPath("$.refreshToken").value("mock.refresh.token"))
                .andExpect(jsonPath("$.username").value("testuser"));
        
        verify(jwtTokenUtil).validateRefreshToken("mock.refresh.token");
        verify(jwtTokenUtil).getUsernameFromToken("mock.refresh.token");
        verify(userDetailsService).loadUserByUsername("testuser");
        verify(jwtTokenUtil).generateToken(mockUserDetails);
    }
    
    @Test
    @DisplayName("토큰 갱신 실패 - 유효하지 않은 토큰")
    void refreshToken_InvalidToken() throws Exception {
        // Given
        String invalidToken = "Bearer invalid.token";
        
        when(jwtTokenUtil.validateRefreshToken(anyString())).thenReturn(false);
        
        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                .header("Authorization", invalidToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("유효하지 않은 Refresh Token입니다."));
        
        verify(jwtTokenUtil).validateRefreshToken("invalid.token");
        verify(userDetailsService, never()).loadUserByUsername(any());
    }
    
    @Test
    @DisplayName("토큰 검증 성공")
    void validateToken_Success() throws Exception {
        // Given
        String validToken = "Bearer valid.access.token";
        
        when(jwtTokenUtil.getUsernameFromToken(anyString())).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(mockUserDetails);
        when(jwtTokenUtil.validateToken(anyString(), any(UserDetails.class))).thenReturn(true);
        
        // When & Then
        mockMvc.perform(post("/api/auth/validate")
                .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.username").value("testuser"));
        
        verify(jwtTokenUtil).getUsernameFromToken("valid.access.token");
        verify(userDetailsService).loadUserByUsername("testuser");
        verify(jwtTokenUtil).validateToken("valid.access.token", mockUserDetails);
    }
    
    @Test
    @DisplayName("로그아웃 처리")
    void logout_Success() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그아웃되었습니다. 클라이언트에서 토큰을 삭제하세요."))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}