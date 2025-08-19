package com.blogapp.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 사용자 정보 수정 요청 DTO
 * 
 * 기존 사용자의 프로필 정보를 수정할 때 사용되는 요청 객체입니다.
 * 비밀번호 변경은 별도의 엔드포인트에서 처리하므로 포함하지 않습니다.
 * 
 * 수정 가능한 정보:
 * - 이메일 주소 (중복 검사 필요)
 * - 이름과 성
 * - 기타 프로필 정보
 * 
 * 비즈니스 규칙:
 * - 사용자명은 변경할 수 없음 (시스템 정책)
 * - 이메일은 중복 검사를 통과해야 함
 * - 모든 필드는 현재 값이 유지되거나 새 값으로 변경됨
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-01-01
 */
@Data
public class UserUpdateRequest {
    
    /**
     * 이메일 주소
     * 
     * 새로운 이메일 주소로 변경합니다.
     * 기존 이메일과 다른 경우 중복 검사를 수행합니다.
     */
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "유효한 이메일 형식이어야 합니다.")
    private String email;
    
    /**
     * 이름 (성)
     * 
     * 사용자의 이름을 변경합니다.
     */
    @NotBlank(message = "이름은 필수 입력값입니다.")
    @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
    private String firstName;
    
    /**
     * 성 (이름)
     * 
     * 사용자의 성을 변경합니다.
     */
    @NotBlank(message = "성은 필수 입력값입니다.")
    @Size(max = 50, message = "성은 50자 이하여야 합니다.")
    private String lastName;
}