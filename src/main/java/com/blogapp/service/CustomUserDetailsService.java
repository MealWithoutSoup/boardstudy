package com.blogapp.service;

import com.blogapp.entity.User;
import com.blogapp.repository.UserRepository;
import com.blogapp.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 커스텀 사용자 상세 정보 서비스
 * 
 * Spring Security의 UserDetailsService 인터페이스를 구현하여
 * 시스템의 사용자 정보를 Spring Security와 연동하는 서비스입니다.
 * 
 * 주요 기능:
 * - 사용자명으로 사용자 정보 조회
 * - User 엔티티를 CustomUserDetails로 변환
 * - 인증 과정에서 사용자 정보 제공
 * - 사용자 존재 여부 및 상태 확인
 * 
 * 설계 특징:
 * - UserRepository를 통한 데이터베이스 접근
 * - @Transactional(readOnly = true)로 읽기 전용 트랜잭션 최적화
 * - 상세한 로깅으로 인증 과정 추적
 * - 예외 처리 및 보안 고려사항 적용
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * 사용자 Repository
     * 
     * 데이터베이스에서 사용자 정보를 조회하기 위한 Repository입니다.
     * JPA Repository를 통해 사용자 엔티티에 접근합니다.
     */
    private final UserRepository userRepository;

    /**
     * 사용자명으로 사용자 상세 정보 로드
     * 
     * Spring Security의 인증 과정에서 호출되는 핵심 메서드입니다.
     * 사용자명(username)을 받아 해당 사용자의 상세 정보를 반환합니다.
     * 
     * 처리 과정:
     * 1. 사용자명으로 데이터베이스에서 사용자 검색
     * 2. 사용자가 존재하지 않으면 UsernameNotFoundException 발생
     * 3. 사용자가 존재하면 CustomUserDetails로 래핑하여 반환
     * 4. 역할(Role) 정보도 함께 로드하여 권한 설정
     * 
     * 보안 고려사항:
     * - 사용자 존재 여부에 대한 정보 노출 최소화
     * - 비활성화된 계정에 대한 처리
     * - 로그인 시도에 대한 상세 로깅
     * 
     * @param username 조회할 사용자명 (로그인 ID)
     * @return UserDetails 구현체 (CustomUserDetails)
     * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("사용자 정보 로드 시작 - 사용자명: {}", username);
        
        try {
            // 1. 사용자명으로 사용자 검색 (역할 정보 함께 로드)
            User user = userRepository.findByUsernameWithRoles(username)
                    .orElseThrow(() -> {
                        log.warn("사용자를 찾을 수 없음 - 사용자명: {}", username);
                        return new UsernameNotFoundException(
                            "사용자를 찾을 수 없습니다: " + username
                        );
                    });
            
            log.debug("사용자 조회 성공 - ID: {}, 사용자명: {}, 활성화 상태: {}, 역할 수: {}", 
                     user.getId(), user.getUsername(), user.getIsActive(), user.getRoles().size());
            
            // 2. 사용자 상태 확인 및 로깅
            if (!user.getIsActive()) {
                log.warn("비활성화된 계정 로그인 시도 - 사용자명: {}", username);
                // 주의: Spring Security는 UserDetails.isEnabled()를 통해 계정 상태를 확인
                // 여기서 예외를 발생시키지 않고 CustomUserDetails에서 처리하도록 함
            }
            
            // 3. 사용자 역할 정보 로깅
            if (user.getRoles().isEmpty()) {
                log.warn("역할이 할당되지 않은 사용자 - 사용자명: {}", username);
            } else {
                String roles = user.getRoles().stream()
                        .map(role -> role.getName())
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("없음");
                log.debug("사용자 역할 정보 - 사용자명: {}, 역할: {}", username, roles);
            }
            
            // 4. CustomUserDetails로 래핑하여 반환
            CustomUserDetails userDetails = new CustomUserDetails(user);
            log.info("사용자 정보 로드 완료 - 사용자명: {}, 권한 수: {}", 
                    username, userDetails.getAuthorities().size());
            
            return userDetails;
            
        } catch (UsernameNotFoundException e) {
            // UsernameNotFoundException은 그대로 전파
            throw e;
            
        } catch (Exception e) {
            // 기타 예외는 로깅 후 UsernameNotFoundException으로 변환
            log.error("사용자 정보 로드 중 예상치 못한 오류 발생 - 사용자명: {}, 오류: {}", 
                     username, e.getMessage(), e);
            throw new UsernameNotFoundException(
                "사용자 정보 로드 중 오류가 발생했습니다: " + username, e
            );
        }
    }

    /**
     * 사용자 존재 여부 확인
     * 
     * 주어진 사용자명의 사용자가 시스템에 존재하는지 확인합니다.
     * 회원가입 시 중복 검사 등에 활용할 수 있습니다.
     * 
     * @param username 확인할 사용자명
     * @return 사용자가 존재하면 true, 없으면 false
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        boolean exists = userRepository.existsByUsername(username);
        log.debug("사용자명 존재 여부 확인 - 사용자명: {}, 존재: {}", username, exists);
        return exists;
    }

    /**
     * 이메일로 사용자 존재 여부 확인
     * 
     * 주어진 이메일의 사용자가 시스템에 존재하는지 확인합니다.
     * 회원가입 시 이메일 중복 검사 등에 활용할 수 있습니다.
     * 
     * @param email 확인할 이메일 주소
     * @return 사용자가 존재하면 true, 없으면 false
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        boolean exists = userRepository.existsByEmail(email);
        log.debug("이메일 존재 여부 확인 - 이메일: {}, 존재: {}", email, exists);
        return exists;
    }

    /**
     * 활성화된 사용자 정보 로드
     * 
     * 사용자명으로 활성화된 사용자만 조회합니다.
     * 비활성화된 계정은 제외됩니다.
     * 
     * @param username 조회할 사용자명
     * @return UserDetails 구현체, 사용자가 없거나 비활성화된 경우 예외
     * @throws UsernameNotFoundException 활성화된 사용자를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public UserDetails loadActiveUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("활성화된 사용자 정보 로드 시작 - 사용자명: {}", username);
        
        User user = userRepository.findByUsernameAndIsActiveTrue(username)
                .orElseThrow(() -> {
                    log.warn("활성화된 사용자를 찾을 수 없음 - 사용자명: {}", username);
                    return new UsernameNotFoundException(
                        "활성화된 사용자를 찾을 수 없습니다: " + username
                    );
                });
        
        log.info("활성화된 사용자 정보 로드 완료 - 사용자명: {}", username);
        return new CustomUserDetails(user);
    }
}