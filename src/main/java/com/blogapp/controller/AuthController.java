package com.blogapp.controller;

import com.blogapp.dto.request.LoginRequest;
import com.blogapp.dto.request.UserRegistrationRequest;
import com.blogapp.dto.response.JwtAuthenticationResponse;
import com.blogapp.dto.response.UserResponse;
import com.blogapp.security.JwtTokenUtil;
import com.blogapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 REST API 컨트롤러
 * 
 * 사용자 인증과 관련된 모든 엔드포인트를 처리하는 컨트롤러입니다.
 * JWT 토큰 기반 인증을 구현하며, 로그인, 회원가입, 토큰 갱신 기능을 제공합니다.
 * 
 * 주요 기능:
 * - 사용자 로그인 및 JWT 토큰 발급
 * - 회원가입 처리
 * - Refresh Token을 통한 Access Token 갱신
 * - 로그아웃 처리
 * - 토큰 유효성 검증
 * 
 * API 설계 원칙:
 * - RESTful API 설계 가이드라인 준수
 * - HTTP 상태 코드 적절한 사용
 * - 일관된 응답 구조
 * - 상세한 API 문서화 (Swagger)
 * - 보안 고려사항 적용
 * 
 * 보안 고려사항:
 * - 비밀번호는 요청/응답에서 제외
 * - JWT 토큰 안전한 관리
 * - 로그인 시도 로깅
 * - 입력값 검증 및 sanitization
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "인증 API", description = "사용자 인증 관련 API")
public class AuthController {

    /**
     * 인증 매니저
     * 
     * Spring Security의 인증 처리를 담당합니다.
     * 사용자명/비밀번호 인증 시 사용됩니다.
     */
    private final AuthenticationManager authenticationManager;

    /**
     * JWT 토큰 유틸리티
     * 
     * JWT 토큰의 생성, 검증, 파싱을 담당합니다.
     */
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * 사용자 상세 정보 서비스
     * 
     * 사용자 정보를 로드하고 UserDetails 객체를 제공합니다.
     */
    private final UserDetailsService userDetailsService;

    /**
     * 사용자 비즈니스 로직 서비스
     * 
     * 회원가입 및 사용자 관리 비즈니스 로직을 처리합니다.
     */
    private final UserService userService;

    // ================================
    // 인증 엔드포인트
    // ================================

    /**
     * 사용자 로그인
     * 
     * 사용자명과 비밀번호로 인증하고 JWT 토큰을 발급합니다.
     * 
     * 처리 과정:
     * 1. 사용자명/비밀번호 검증
     * 2. 인증 성공 시 JWT 토큰 생성
     * 3. Access Token과 Refresh Token 반환
     * 4. 로그인 기록 로깅
     * 
     * @param loginRequest 로그인 요청 정보 (사용자명, 비밀번호)
     * @return JWT 토큰 정보와 사용자 기본 정보
     */
    @PostMapping("/login")
    @Operation(
        summary = "사용자 로그인",
        description = "사용자명과 비밀번호로 로그인하고 JWT 토큰을 발급받습니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패 - 잘못된 사용자명 또는 비밀번호"),
        @ApiResponse(responseCode = "403", description = "계정 비활성화"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 형식")
    })
    public ResponseEntity<JwtAuthenticationResponse> login(
            @Parameter(description = "로그인 요청 정보", required = true)
            @Valid @RequestBody LoginRequest loginRequest) {
        
        log.info("로그인 시도 - 사용자명: {}", loginRequest.getUsername());
        
        try {
            // 1. 사용자명/비밀번호 인증
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
            
            // 2. 인증된 사용자 정보 로드
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            // 3. JWT 토큰 생성
            String accessToken = jwtTokenUtil.generateToken(userDetails);
            String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);
            
            // 4. 응답 데이터 구성
            JwtAuthenticationResponse response = JwtAuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .username(userDetails.getUsername())
                    .authorities(userDetails.getAuthorities().stream()
                               .map(auth -> auth.getAuthority())
                               .toList())
                    .build();
            
            log.info("로그인 성공 - 사용자명: {}, 권한: {}", 
                    userDetails.getUsername(), userDetails.getAuthorities());
            
            return ResponseEntity.ok(response);
            
        } catch (BadCredentialsException e) {
            log.warn("로그인 실패 - 잘못된 인증 정보: {}", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(JwtAuthenticationResponse.builder()
                          .error("잘못된 사용자명 또는 비밀번호입니다.")
                          .build());
            
        } catch (DisabledException e) {
            log.warn("로그인 실패 - 비활성화된 계정: {}", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(JwtAuthenticationResponse.builder()
                          .error("비활성화된 계정입니다. 관리자에게 문의하세요.")
                          .build());
            
        } catch (Exception e) {
            log.error("로그인 처리 중 오류 발생 - 사용자명: {}, 오류: {}", 
                     loginRequest.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(JwtAuthenticationResponse.builder()
                          .error("로그인 처리 중 오류가 발생했습니다.")
                          .build());
        }
    }

    /**
     * 사용자 회원가입
     * 
     * 새로운 사용자 계정을 생성합니다.
     * 
     * 처리 과정:
     * 1. 입력값 검증 (유효성 검사)
     * 2. 중복 검사 (사용자명, 이메일)
     * 3. 사용자 등록 처리
     * 4. 기본 역할(USER) 할당
     * 5. 등록 완료 정보 반환
     * 
     * @param registrationRequest 회원가입 요청 정보
     * @return 등록된 사용자 정보 (비밀번호 제외)
     */
    @PostMapping("/register")
    @Operation(
        summary = "사용자 회원가입",
        description = "새로운 사용자 계정을 생성합니다. 기본적으로 USER 역할이 할당됩니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "회원가입 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 - 입력값 검증 실패"),
        @ApiResponse(responseCode = "409", description = "중복된 사용자명 또는 이메일"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<UserResponse> register(
            @Parameter(description = "회원가입 요청 정보", required = true)
            @Valid @RequestBody UserRegistrationRequest registrationRequest) {
        
        log.info("회원가입 시도 - 사용자명: {}, 이메일: {}", 
                registrationRequest.getUsername(), registrationRequest.getEmail());
        
        try {
            // 사용자 등록 처리
            UserResponse userResponse = userService.registerUser(registrationRequest);
            
            log.info("회원가입 성공 - 사용자 ID: {}, 사용자명: {}", 
                    userResponse.getId(), userResponse.getUsername());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
            
        } catch (Exception e) {
            log.error("회원가입 처리 중 오류 발생 - 사용자명: {}, 오류: {}", 
                     registrationRequest.getUsername(), e.getMessage(), e);
            throw e; // GlobalExceptionHandler에서 처리
        }
    }

    // ================================
    // 토큰 관리 엔드포인트
    // ================================

    /**
     * Access Token 갱신
     * 
     * Refresh Token을 사용하여 새로운 Access Token을 발급합니다.
     * 
     * 처리 과정:
     * 1. Refresh Token 유효성 검증
     * 2. 토큰에서 사용자 정보 추출
     * 3. 새로운 Access Token 생성
     * 4. 새로운 토큰 정보 반환
     * 
     * @param refreshToken Refresh Token (Bearer 형식)
     * @return 새로운 JWT 토큰 정보
     */
    @PostMapping("/refresh")
    @Operation(
        summary = "Access Token 갱신",
        description = "Refresh Token을 사용하여 새로운 Access Token을 발급받습니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 형식")
    })
    public ResponseEntity<JwtAuthenticationResponse> refreshToken(
            @Parameter(description = "Refresh Token (Bearer 포함)", required = true)
            @RequestHeader("Authorization") String refreshToken) {
        
        log.debug("토큰 갱신 요청");
        
        try {
            // Bearer 접두사 제거
            if (refreshToken.startsWith("Bearer ")) {
                refreshToken = refreshToken.substring(7);
            }
            
            // 1. Refresh Token 유효성 검증
            if (!jwtTokenUtil.validateRefreshToken(refreshToken)) {
                log.warn("유효하지 않은 Refresh Token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(JwtAuthenticationResponse.builder()
                              .error("유효하지 않은 Refresh Token입니다.")
                              .build());
            }
            
            // 2. 토큰에서 사용자명 추출
            String username = jwtTokenUtil.getUsernameFromToken(refreshToken);
            
            // 3. 사용자 정보 로드
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            // 4. 새로운 Access Token 생성
            String newAccessToken = jwtTokenUtil.generateToken(userDetails);
            
            // 5. 응답 데이터 구성
            JwtAuthenticationResponse response = JwtAuthenticationResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken) // 기존 Refresh Token 유지
                    .tokenType("Bearer")
                    .username(userDetails.getUsername())
                    .authorities(userDetails.getAuthorities().stream()
                               .map(auth -> auth.getAuthority())
                               .toList())
                    .build();
            
            log.info("토큰 갱신 성공 - 사용자명: {}", username);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("토큰 갱신 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(JwtAuthenticationResponse.builder()
                          .error("토큰 갱신에 실패했습니다.")
                          .build());
        }
    }

    /**
     * 토큰 유효성 검증
     * 
     * 클라이언트가 보유한 토큰이 유효한지 확인합니다.
     * 
     * @param token 검증할 토큰 (Bearer 형식)
     * @return 토큰 유효성 정보
     */
    @PostMapping("/validate")
    @Operation(
        summary = "토큰 유효성 검증",
        description = "Access Token의 유효성을 검증합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "토큰 유효"),
        @ApiResponse(responseCode = "401", description = "토큰 무효 또는 만료")
    })
    public ResponseEntity<?> validateToken(
            @Parameter(description = "검증할 토큰 (Bearer 포함)", required = true)
            @RequestHeader("Authorization") String token) {
        
        try {
            // Bearer 접두사 제거
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            // 토큰에서 사용자명 추출
            String username = jwtTokenUtil.getUsernameFromToken(token);
            
            // 사용자 정보 로드 및 토큰 검증
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            boolean isValid = jwtTokenUtil.validateToken(token, userDetails);
            
            if (isValid) {
                return ResponseEntity.ok().body(Map.of(
                    "valid", true,
                    "username", username,
                    "authorities", userDetails.getAuthorities().stream()
                                  .map(auth -> auth.getAuthority())
                                  .toList()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("valid", false, "message", "토큰이 유효하지 않습니다."));
            }
            
        } catch (Exception e) {
            log.warn("토큰 검증 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "message", "토큰 검증에 실패했습니다."));
        }
    }

    /**
     * 로그아웃
     * 
     * 클라이언트 측에서 토큰을 삭제하도록 안내합니다.
     * JWT는 stateless하므로 서버에서 토큰을 무효화할 수 없습니다.
     * 
     * 향후 개선 사항:
     * - Redis를 활용한 토큰 블랙리스트 구현
     * - 토큰 만료 시간 단축
     * 
     * @return 로그아웃 완료 메시지
     */
    @PostMapping("/logout")
    @Operation(
        summary = "사용자 로그아웃",
        description = "현재 세션을 종료합니다. 클라이언트에서 토큰을 삭제해야 합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    })
    public ResponseEntity<?> logout() {
        log.info("로그아웃 요청");
        
        // JWT는 stateless하므로 서버에서 직접 무효화할 수 없음
        // 클라이언트에서 토큰을 삭제하도록 안내
        return ResponseEntity.ok().body(Map.of(
            "message", "로그아웃되었습니다. 클라이언트에서 토큰을 삭제하세요.",
            "timestamp", System.currentTimeMillis()
        ));
    }

    // ================================
    // 사용자 정보 조회 엔드포인트
    // ================================

    /**
     * 현재 로그인한 사용자 정보 조회
     * 
     * JWT 토큰을 통해 인증된 현재 사용자의 정보를 반환합니다.
     * 
     * @param authentication Spring Security 인증 객체
     * @return 현재 사용자 정보
     */
    @GetMapping("/me")
    @Operation(
        summary = "현재 사용자 정보 조회",
        description = "JWT 토큰으로 인증된 현재 사용자의 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        log.debug("현재 사용자 정보 조회 - 사용자명: {}", authentication.getName());
        
        try {
            UserResponse userResponse = userService.getUserByUsername(authentication.getName());
            return ResponseEntity.ok(userResponse);
            
        } catch (Exception e) {
            log.error("현재 사용자 정보 조회 실패: {}", e.getMessage());
            throw e; // GlobalExceptionHandler에서 처리
        }
    }
}