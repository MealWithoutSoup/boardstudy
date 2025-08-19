package com.blogapp.security;

import com.blogapp.entity.Role;
import com.blogapp.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 커스텀 사용자 상세 정보 클래스
 * 
 * Spring Security의 UserDetails 인터페이스를 구현하여
 * 시스템의 User 엔티티와 Spring Security를 연동하는 클래스입니다.
 * 
 * 주요 기능:
 * - User 엔티티를 Spring Security UserDetails로 변환
 * - 사용자 권한 정보를 GrantedAuthority로 매핑
 * - 계정 상태 정보 제공 (활성화, 만료, 잠금 등)
 * - 인증 및 인가 과정에서 사용자 정보 제공
 * 
 * 설계 특징:
 * - User 엔티티를 래핑하여 UserDetails 구현
 * - 역할(Role) 정보를 Spring Security 권한으로 변환
 * - 계정 상태는 User 엔티티의 isActive 필드 활용
 * - 불변 객체로 설계하여 안전성 보장
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-01-01
 */
@RequiredArgsConstructor
@Getter
public class CustomUserDetails implements UserDetails {

    /**
     * 래핑된 사용자 엔티티
     * 
     * 시스템의 User 엔티티를 래핑하여 UserDetails 인터페이스를 구현합니다.
     * 이를 통해 JPA 엔티티와 Spring Security를 연동할 수 있습니다.
     */
    private final User user;

    /**
     * 사용자 권한 목록 반환
     * 
     * 사용자가 가진 역할(Role)들을 Spring Security의 GrantedAuthority로 변환하여 반환합니다.
     * 
     * 변환 규칙:
     * - Role의 name 필드를 그대로 사용
     * - "ROLE_" 접두사는 추가하지 않음 (이미 "ADMIN", "USER" 형태로 저장)
     * - SimpleGrantedAuthority로 래핑하여 반환
     * 
     * 예시:
     * - 사용자가 "ADMIN", "USER" 역할을 가진 경우
     * - [SimpleGrantedAuthority("ADMIN"), SimpleGrantedAuthority("USER")] 반환
     * 
     * @return 사용자 권한 목록
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<Role> roles = user.getRoles();
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());
    }

    /**
     * 사용자 비밀번호 반환
     * 
     * 인증 과정에서 사용할 사용자의 암호화된 비밀번호를 반환합니다.
     * 
     * 보안 특징:
     * - 이미 BCrypt 등으로 암호화된 비밀번호 반환
     * - Spring Security가 PasswordEncoder를 통해 검증
     * - 평문 비밀번호는 절대 반환하지 않음
     * 
     * @return 암호화된 비밀번호
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * 사용자명 반환
     * 
     * 인증 과정에서 사용할 사용자의 고유 식별자를 반환합니다.
     * 
     * 특징:
     * - User 엔티티의 username 필드 사용
     * - 로그인 시 입력하는 아이디와 일치
     * - 시스템 내에서 유일한 값
     * 
     * @return 사용자명 (로그인 아이디)
     */
    @Override
    public String getUsername() {
        return user.getUsername();
    }

    /**
     * 계정 만료 여부 확인
     * 
     * 사용자 계정이 만료되지 않았는지 확인합니다.
     * 
     * 현재 구현:
     * - 항상 true 반환 (계정 만료 기능 미구현)
     * - 추후 User 엔티티에 만료 날짜 필드 추가 시 활용 가능
     * 
     * 확장 방안:
     * - User 엔티티에 accountExpiryDate 필드 추가
     * - 현재 날짜와 비교하여 만료 여부 판단
     * - 관리자 기능으로 계정 만료 날짜 설정
     * 
     * @return 계정이 만료되지 않았으면 true, 만료되었으면 false
     */
    @Override
    public boolean isAccountNonExpired() {
        // 현재는 계정 만료 기능을 구현하지 않으므로 항상 true 반환
        // 추후 User 엔티티에 만료 날짜 필드를 추가하여 확장 가능
        return true;
    }

    /**
     * 계정 잠김 여부 확인
     * 
     * 사용자 계정이 잠겨있지 않은지 확인합니다.
     * 
     * 현재 구현:
     * - 항상 true 반환 (계정 잠금 기능 미구현)
     * - 추후 User 엔티티에 잠금 상태 필드 추가 시 활용 가능
     * 
     * 확장 방안:
     * - User 엔티티에 accountLocked 필드 추가
     * - 로그인 실패 횟수 기반 자동 잠금 기능
     * - 관리자 기능으로 수동 계정 잠금/해제
     * 
     * @return 계정이 잠겨있지 않으면 true, 잠겨있으면 false
     */
    @Override
    public boolean isAccountNonLocked() {
        // 현재는 계정 잠금 기능을 구현하지 않으므로 항상 true 반환
        // 추후 로그인 실패 횟수 기반 계정 잠금 기능 추가 가능
        return true;
    }

    /**
     * 자격 증명 만료 여부 확인
     * 
     * 사용자의 자격 증명(비밀번호)이 만료되지 않았는지 확인합니다.
     * 
     * 현재 구현:
     * - 항상 true 반환 (비밀번호 만료 기능 미구현)
     * - 추후 User 엔티티에 비밀번호 변경 날짜 필드 추가 시 활용 가능
     * 
     * 확장 방안:
     * - User 엔티티에 passwordLastChanged 필드 추가
     * - 보안 정책에 따른 주기적 비밀번호 변경 강제
     * - 비밀번호 만료 알림 기능
     * 
     * @return 자격 증명이 만료되지 않았으면 true, 만료되었으면 false
     */
    @Override
    public boolean isCredentialsNonExpired() {
        // 현재는 비밀번호 만료 기능을 구현하지 않으므로 항상 true 반환
        // 추후 비밀번호 정책에 따른 만료 기능 추가 가능
        return true;
    }

    /**
     * 계정 활성화 여부 확인
     * 
     * 사용자 계정이 활성화되어 있는지 확인합니다.
     * 
     * 구현:
     * - User 엔티티의 isActive 필드 사용
     * - true: 활성화된 계정 (로그인 가능)
     * - false: 비활성화된 계정 (로그인 불가)
     * 
     * 활용 사례:
     * - 계정 정지 처리
     * - 이메일 인증 미완료 사용자
     * - 관리자에 의한 계정 비활성화
     * 
     * @return 계정이 활성화되어 있으면 true, 비활성화되어 있으면 false
     */
    @Override
    public boolean isEnabled() {
        return user.getIsActive() != null && user.getIsActive();
    }

    /**
     * 사용자 ID 반환
     * 
     * User 엔티티의 기본키 ID를 반환합니다.
     * 추가 사용자 정보가 필요한 경우 활용할 수 있습니다.
     * 
     * @return 사용자 ID
     */
    public Long getId() {
        return user.getId();
    }

    /**
     * 사용자 이메일 반환
     * 
     * User 엔티티의 이메일 주소를 반환합니다.
     * 추가 사용자 정보가 필요한 경우 활용할 수 있습니다.
     * 
     * @return 사용자 이메일
     */
    public String getEmail() {
        return user.getEmail();
    }

    /**
     * 사용자 전체 이름 반환
     * 
     * User 엔티티의 전체 이름을 반환합니다.
     * UI에서 사용자 표시명으로 활용할 수 있습니다.
     * 
     * @return 사용자 전체 이름
     */
    public String getFullName() {
        return user.getFullName();
    }

    /**
     * 특정 권한 보유 여부 확인
     * 
     * 사용자가 특정 권한을 가지고 있는지 확인합니다.
     * 
     * @param authority 확인할 권한명 (예: "ADMIN", "USER")
     * @return 권한을 가지고 있으면 true, 없으면 false
     */
    public boolean hasAuthority(String authority) {
        return getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));
    }

    /**
     * 관리자 권한 보유 여부 확인
     * 
     * 사용자가 관리자 권한을 가지고 있는지 확인합니다.
     * 
     * @return 관리자 권한이 있으면 true, 없으면 false
     */
    public boolean isAdmin() {
        return hasAuthority("ADMIN");
    }

    /**
     * 일반 사용자 권한 보유 여부 확인
     * 
     * 사용자가 일반 사용자 권한을 가지고 있는지 확인합니다.
     * 
     * @return 일반 사용자 권한이 있으면 true, 없으면 false
     */
    public boolean isUser() {
        return hasAuthority("USER");
    }
}