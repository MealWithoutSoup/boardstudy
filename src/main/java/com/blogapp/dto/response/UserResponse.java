package com.blogapp.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자 정보 응답 DTO
 * 
 * 클라이언트에게 사용자 정보를 전달할 때 사용되는 응답 객체입니다.
 * 보안상 민감한 정보(비밀번호 등)는 제외하고 필요한 정보만 포함합니다.
 * 
 * 포함 정보:
 * - 기본 사용자 정보 (ID, 사용자명, 이메일, 이름)
 * - 계정 상태 (활성화 여부)
 * - 사용자 역할 목록
 * - 계정 생성/수정 시간
 * 
 * 보안 고려사항:
 * - 비밀번호는 절대 포함하지 않음
 * - 내부 시스템 정보는 노출하지 않음
 * - 사용자 권한에 따라 필터링 가능
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-01-01
 */
@Data
public class UserResponse {
    
    /**
     * 사용자 고유 식별자
     * 
     * 데이터베이스의 기본키로 사용되는 고유 ID입니다.
     */
    private Long id;
    
    /**
     * 사용자명
     * 
     * 로그인 시 사용되는 고유한 사용자명입니다.
     */
    private String username;
    
    /**
     * 이메일 주소
     * 
     * 사용자의 연락처 이메일 주소입니다.
     */
    private String email;
    
    /**
     * 이름 (성)
     * 
     * 사용자의 실제 이름입니다.
     */
    private String firstName;
    
    /**
     * 성 (이름)
     * 
     * 사용자의 실제 성입니다.
     */
    private String lastName;
    
    /**
     * 계정 활성화 상태
     * 
     * 계정이 활성화되어 있는지 여부를 나타냅니다.
     * false인 경우 로그인이 제한됩니다.
     */
    private Boolean isActive;
    
    /**
     * 사용자 역할 목록
     * 
     * 사용자가 가지고 있는 역할들의 이름 목록입니다.
     * 권한 제어에 사용됩니다.
     */
    private List<String> roles;
    
    /**
     * 계정 생성 시간
     * 
     * 사용자 계정이 처음 생성된 시간입니다.
     */
    private LocalDateTime createdAt;
    
    /**
     * 마지막 수정 시간
     * 
     * 사용자 정보가 마지막으로 수정된 시간입니다.
     */
    private LocalDateTime updatedAt;
    
    /**
     * 전체 이름 반환
     * 
     * 이름과 성을 결합하여 전체 이름을 반환하는 헬퍼 메서드입니다.
     * 
     * @return 전체 이름 (성 + 이름)
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    /**
     * 관리자 여부 확인
     * 
     * 사용자가 관리자 역할을 가지고 있는지 확인합니다.
     * 
     * @return 관리자 역할 보유 여부
     */
    public boolean isAdmin() {
        return roles != null && roles.contains("ADMIN");
    }
}