package com.blogapp.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * JWT 인증 응답 DTO
 * 
 * 로그인 성공 시 클라이언트에게 전달되는 JWT 토큰 정보를 담는 응답 객체입니다.
 * Access Token과 Refresh Token, 사용자 기본 정보를 포함합니다.
 * 
 * 포함 정보:
 * - Access Token: API 호출 시 사용하는 인증 토큰
 * - Refresh Token: Access Token 갱신을 위한 토큰
 * - 토큰 타입: 일반적으로 "Bearer"
 * - 사용자 기본 정보: 사용자명, 권한 목록
 * - 에러 정보: 인증 실패 시 에러 메시지
 * 
 * 보안 고려사항:
 * - 토큰은 안전한 저장소에 보관 필요
 * - HTTPS를 통해서만 전송
 * - 토큰 만료 시간 확인 및 갱신 필요
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Builder
public class JwtAuthenticationResponse {
    
    /**
     * Access Token
     * 
     * API 호출 시 Authorization 헤더에 포함하여 인증에 사용하는 토큰입니다.
     * 만료 시간이 짧게 설정되어 보안성을 높입니다.
     */
    private String accessToken;
    
    /**
     * Refresh Token
     * 
     * Access Token이 만료되었을 때 새로운 Access Token을 발급받기 위한 토큰입니다.
     * Access Token보다 긴 만료 시간을 가집니다.
     */
    private String refreshToken;
    
    /**
     * 토큰 타입
     * 
     * 토큰의 종류를 나타내는 문자열로, 일반적으로 "Bearer"가 사용됩니다.
     * Authorization 헤더 형식: "Bearer {accessToken}"
     */
    private String tokenType;
    
    /**
     * 사용자명
     * 
     * 인증된 사용자의 사용자명입니다.
     * 클라이언트에서 사용자 식별에 사용할 수 있습니다.
     */
    private String username;
    
    /**
     * 사용자 권한 목록
     * 
     * 인증된 사용자가 가지고 있는 권한(역할) 목록입니다.
     * 클라이언트에서 UI 제어나 기능 접근 제어에 사용할 수 있습니다.
     */
    private List<String> authorities;
    
    /**
     * 에러 메시지
     * 
     * 인증 실패나 토큰 처리 오류 시 클라이언트에게 전달할 에러 메시지입니다.
     * 성공 시에는 null 또는 빈 값을 가집니다.
     */
    private String error;
    
    /**
     * 토큰 만료 시간 (초)
     * 
     * Access Token의 만료까지 남은 시간을 초 단위로 나타냅니다.
     * 클라이언트에서 토큰 갱신 시점을 결정하는 데 사용할 수 있습니다.
     */
    private Long expiresIn;
    
    /**
     * 인증 성공 여부 확인
     * 
     * 에러 메시지가 없고 Access Token이 존재하면 성공으로 판단합니다.
     * 
     * @return 인증 성공 여부
     */
    public boolean isSuccess() {
        return error == null && accessToken != null && !accessToken.isEmpty();
    }
}