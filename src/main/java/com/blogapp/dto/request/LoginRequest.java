package com.blogapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 로그인 요청 DTO
 * 
 * 사용자 로그인 시 클라이언트에서 서버로 전송되는 인증 정보를 담는 객체입니다.
 * 사용자명과 비밀번호를 포함하며, JWT 토큰 발급의 기초 데이터가 됩니다.
 * 
 * 보안 고려사항:
 * - 비밀번호는 평문으로 전송되므로 반드시 HTTPS 사용
 * - 로그인 시도는 보안 로그에 기록
 * - 비밀번호는 toString에서 제외하여 로그 노출 방지
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-01-01
 */
@Data
public class LoginRequest {
    
    /**
     * 사용자명
     * 
     * 로그인 시 사용되는 사용자 식별자입니다.
     * 이메일 또는 사용자명 모두 허용할 수 있도록 설계되었습니다.
     */
    @NotBlank(message = "사용자명은 필수 입력값입니다.")
    private String username;
    
    /**
     * 비밀번호
     * 
     * 사용자 인증을 위한 비밀번호입니다.
     * 서버에서 BCrypt로 암호화된 값과 비교됩니다.
     */
    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    private String password;
    
    /**
     * toString 메서드 오버라이드
     * 
     * 보안상 비밀번호는 로그에 노출되지 않도록 마스킹 처리합니다.
     * 
     * @return 비밀번호가 마스킹된 문자열 표현
     */
    @Override
    public String toString() {
        return "LoginRequest{" +
                "username='" + username + '\'' +
                ", password='[MASKED]'" +
                '}';
    }
}