package com.blogapp.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 사용자 회원가입 요청 DTO
 * 
 * 클라이언트에서 회원가입 시 전송하는 데이터를 담는 객체입니다.
 * Bean Validation을 통해 입력값 검증을 수행합니다.
 * 
 * 주요 검증 규칙:
 * - 사용자명: 3~20자, 필수값
 * - 이메일: 유효한 이메일 형식, 필수값
 * - 비밀번호: 8~100자, 필수값
 * - 이름: 1~50자, 필수값
 * 
 * 보안 고려사항:
 * - 비밀번호는 평문으로 전송되지만 HTTPS 사용 필수
 * - 서버에서 추가 검증 및 암호화 처리
 * - 민감한 정보는 로그에 기록하지 않음
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-01-01
 */
@Data
public class UserRegistrationRequest {
    
    /**
     * 사용자명
     * 
     * 시스템에서 사용자를 식별하는 고유한 이름입니다.
     * 로그인 시 사용되며, 중복될 수 없습니다.
     */
    @NotBlank(message = "사용자명은 필수 입력값입니다.")
    @Size(min = 3, max = 20, message = "사용자명은 3자 이상 20자 이하여야 합니다.")
    private String username;
    
    /**
     * 이메일 주소
     * 
     * 사용자의 연락처이며, 비밀번호 재설정 등에 사용됩니다.
     * 유효한 이메일 형식이어야 하며, 중복될 수 없습니다.
     */
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "유효한 이메일 형식이어야 합니다.")
    private String email;
    
    /**
     * 비밀번호
     * 
     * 사용자 인증을 위한 비밀번호입니다.
     * 서버에서 BCrypt로 암호화되어 저장됩니다.
     */
    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다.")
    private String password;
    
    /**
     * 이름 (성)
     * 
     * 사용자의 실제 이름입니다.
     */
    @NotBlank(message = "이름은 필수 입력값입니다.")
    @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
    private String firstName;
    
    /**
     * 성 (이름)
     * 
     * 사용자의 실제 성입니다.
     */
    @NotBlank(message = "성은 필수 입력값입니다.")
    @Size(max = 50, message = "성은 50자 이하여야 합니다.")
    private String lastName;
}