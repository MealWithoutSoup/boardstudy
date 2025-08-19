package com.blogapp.service;

import com.blogapp.dto.request.UserRegistrationRequest;
import com.blogapp.dto.request.UserUpdateRequest;
import com.blogapp.dto.response.UserResponse;
import com.blogapp.entity.Role;
import com.blogapp.entity.User;
import com.blogapp.exception.DuplicateEmailException;
import com.blogapp.exception.DuplicateUsernameException;
import com.blogapp.exception.UserNotFoundException;
import com.blogapp.repository.RoleRepository;
import com.blogapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 사용자 비즈니스 로직 서비스
 * 
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 회원가입, 프로필 관리, 사용자 검색 등의 기능을 제공합니다.
 * 
 * 주요 기능:
 * - 회원가입 및 사용자 등록
 * - 사용자 정보 수정 및 삭제
 * - 사용자 목록 조회 및 검색
 * - 비밀번호 변경 및 계정 관리
 * - 역할 관리 및 권한 설정
 * - 사용자 통계 및 활동 분석
 * 
 * 설계 원칙:
 * - 단일 책임 원칙: 사용자 관련 비즈니스 로직만 담당
 * - 트랜잭션 관리: @Transactional로 데이터 일관성 보장
 * - 예외 처리: 도메인별 커스텀 예외 사용
 * - 보안: 비밀번호 암호화 및 권한 검증
 * - 로깅: 중요 작업에 대한 상세 로깅
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    /**
     * 사용자 Repository
     * 
     * 데이터베이스에서 사용자 정보를 조회, 저장, 수정, 삭제하는 Repository입니다.
     */
    private final UserRepository userRepository;

    /**
     * 역할 Repository
     * 
     * 사용자 역할 정보를 관리하는 Repository입니다.
     * 회원가입 시 기본 역할 할당에 사용됩니다.
     */
    private final RoleRepository roleRepository;

    /**
     * 비밀번호 암호화 도구
     * 
     * BCrypt 알고리즘을 사용하여 비밀번호를 안전하게 암호화합니다.
     * 회원가입 및 비밀번호 변경 시 사용됩니다.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * 객체 매핑 도구
     * 
     * Entity와 DTO 간의 변환을 자동화하는 ModelMapper입니다.
     * 반복적인 매핑 코드를 줄이고 유지보수성을 향상시킵니다.
     */
    private final ModelMapper modelMapper;

    // ================================
    // 회원가입 및 사용자 등록
    // ================================

    /**
     * 새로운 사용자 등록 (회원가입)
     * 
     * 회원가입 요청을 처리하여 새로운 사용자를 시스템에 등록합니다.
     * 
     * 처리 과정:
     * 1. 사용자명과 이메일 중복 검사
     * 2. 비밀번호 암호화
     * 3. 기본 역할(USER) 할당
     * 4. 사용자 정보 저장
     * 5. 응답 DTO 생성 및 반환
     * 
     * 비즈니스 규칙:
     * - 사용자명은 시스템 내에서 유일해야 함
     * - 이메일 주소는 시스템 내에서 유일해야 함
     * - 비밀번호는 BCrypt로 암호화하여 저장
     * - 신규 사용자는 기본적으로 USER 역할을 가짐
     * - 계정은 기본적으로 활성화 상태로 생성
     * 
     * @param request 회원가입 요청 정보
     * @return 등록된 사용자 정보 (비밀번호 제외)
     * @throws DuplicateUsernameException 사용자명이 이미 존재하는 경우
     * @throws DuplicateEmailException 이메일이 이미 존재하는 경우
     */
    @Transactional
    public UserResponse registerUser(UserRegistrationRequest request) {
        log.info("사용자 등록 시작 - 사용자명: {}, 이메일: {}", request.getUsername(), request.getEmail());
        
        // 1. 중복 검사
        validateUserUniqueness(request.getUsername(), request.getEmail());
        
        // 2. 사용자 엔티티 생성
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .isActive(true)
                .build();
        
        // 3. 기본 역할(USER) 할당
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> {
                    log.error("기본 역할 'USER'를 찾을 수 없습니다.");
                    return new RuntimeException("시스템 초기화 오류: USER 역할이 존재하지 않습니다.");
                });
        user.addRole(userRole);
        
        // 4. 사용자 저장
        User savedUser = userRepository.save(user);
        log.info("사용자 등록 완료 - ID: {}, 사용자명: {}", savedUser.getId(), savedUser.getUsername());
        
        // 5. 응답 DTO 변환 및 반환
        return convertToUserResponse(savedUser);
    }

    /**
     * 관리자용 사용자 등록
     * 
     * 관리자가 특정 역할을 가진 사용자를 직접 등록할 때 사용합니다.
     * 일반 회원가입과 달리 역할을 지정할 수 있습니다.
     * 
     * @param request 사용자 등록 요청 정보
     * @param roleNames 할당할 역할명 목록
     * @return 등록된 사용자 정보
     */
    @Transactional
    public UserResponse registerUserWithRoles(UserRegistrationRequest request, List<String> roleNames) {
        log.info("관리자용 사용자 등록 시작 - 사용자명: {}, 역할: {}", 
                request.getUsername(), String.join(", ", roleNames));
        
        // 중복 검사
        validateUserUniqueness(request.getUsername(), request.getEmail());
        
        // 사용자 엔티티 생성
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .isActive(true)
                .build();
        
        // 지정된 역할들 할당
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("역할을 찾을 수 없습니다: " + roleName));
            user.addRole(role);
        }
        
        User savedUser = userRepository.save(user);
        log.info("관리자용 사용자 등록 완료 - ID: {}, 역할 수: {}", 
                savedUser.getId(), savedUser.getRoles().size());
        
        return convertToUserResponse(savedUser);
    }

    // ================================
    // 사용자 조회 및 검색
    // ================================

    /**
     * 사용자 ID로 조회
     * 
     * 특정 사용자의 상세 정보를 조회합니다.
     * 
     * @param userId 조회할 사용자 ID
     * @return 사용자 정보
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    public UserResponse getUserById(Long userId) {
        log.debug("사용자 조회 - ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("사용자를 찾을 수 없음 - ID: {}", userId);
                    return new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId);
                });
        
        return convertToUserResponse(user);
    }

    /**
     * 사용자명으로 조회
     * 
     * 사용자명을 통해 사용자 정보를 조회합니다.
     * 
     * @param username 조회할 사용자명
     * @return 사용자 정보
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    public UserResponse getUserByUsername(String username) {
        log.debug("사용자 조회 - 사용자명: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("사용자를 찾을 수 없음 - 사용자명: {}", username);
                    return new UserNotFoundException("사용자를 찾을 수 없습니다. 사용자명: " + username);
                });
        
        return convertToUserResponse(user);
    }

    /**
     * 모든 사용자 목록 조회 (페이징)
     * 
     * 관리자 페이지에서 사용자 목록을 조회할 때 사용합니다.
     * 페이징과 정렬을 지원합니다.
     * 
     * @param pageable 페이징 정보 (페이지 번호, 크기, 정렬)
     * @return 사용자 목록 (페이징 처리됨)
     */
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.debug("사용자 목록 조회 - 페이지: {}, 크기: {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::convertToUserResponse);
    }

    /**
     * 활성화된 사용자 목록 조회
     * 
     * 활성화된 사용자만 조회합니다.
     * 
     * @param pageable 페이징 정보
     * @return 활성화된 사용자 목록
     */
    public Page<UserResponse> getActiveUsers(Pageable pageable) {
        log.debug("활성화된 사용자 목록 조회");
        
        Page<User> users = userRepository.findByIsActiveTrue(pageable);
        return users.map(this::convertToUserResponse);
    }

    /**
     * 사용자 검색
     * 
     * 사용자명이나 이메일로 사용자를 검색합니다.
     * 
     * @param searchTerm 검색어
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    public Page<UserResponse> searchUsers(String searchTerm, Pageable pageable) {
        log.debug("사용자 검색 - 검색어: {}", searchTerm);
        
        Page<User> users = userRepository.searchByUsernameOrEmail(searchTerm, pageable);
        return users.map(this::convertToUserResponse);
    }

    // ================================
    // 사용자 정보 수정
    // ================================

    /**
     * 사용자 정보 수정
     * 
     * 사용자의 기본 정보를 수정합니다.
     * 비밀번호 변경은 별도 메서드를 사용합니다.
     * 
     * @param userId 수정할 사용자 ID
     * @param request 수정할 정보
     * @return 수정된 사용자 정보
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     * @throws DuplicateEmailException 이메일이 중복되는 경우
     */
    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        log.info("사용자 정보 수정 시작 - ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
        
        // 이메일 중복 검사 (본인 이메일 제외)
        if (!user.getEmail().equals(request.getEmail()) && 
            userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다: " + request.getEmail());
        }
        
        // 정보 업데이트
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        
        User updatedUser = userRepository.save(user);
        log.info("사용자 정보 수정 완료 - ID: {}", userId);
        
        return convertToUserResponse(updatedUser);
    }

    /**
     * 비밀번호 변경
     * 
     * 사용자의 비밀번호를 변경합니다.
     * 현재 비밀번호 확인 후 새 비밀번호로 변경합니다.
     * 
     * @param userId 사용자 ID
     * @param currentPassword 현재 비밀번호
     * @param newPassword 새 비밀번호
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     * @throws IllegalArgumentException 현재 비밀번호가 틀린 경우
     */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        log.info("비밀번호 변경 시작 - 사용자 ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
        
        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            log.warn("비밀번호 변경 실패 - 현재 비밀번호 불일치, 사용자 ID: {}", userId);
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }
        
        // 새 비밀번호 암호화 및 저장
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("비밀번호 변경 완료 - 사용자 ID: {}", userId);
    }

    // ================================
    // 계정 관리
    // ================================

    /**
     * 사용자 계정 활성화/비활성화
     * 
     * 관리자가 사용자 계정의 활성화 상태를 변경합니다.
     * 
     * @param userId 사용자 ID
     * @param isActive 활성화 상태
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Transactional
    public void updateUserActiveStatus(Long userId, Boolean isActive) {
        log.info("사용자 활성화 상태 변경 - ID: {}, 활성화: {}", userId, isActive);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
        
        user.setIsActive(isActive);
        userRepository.save(user);
        
        log.info("사용자 활성화 상태 변경 완료 - ID: {}", userId);
    }

    /**
     * 사용자 삭제
     * 
     * 사용자를 시스템에서 삭제합니다.
     * 실제로는 soft delete를 권장합니다.
     * 
     * @param userId 삭제할 사용자 ID
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Transactional
    public void deleteUser(Long userId) {
        log.info("사용자 삭제 시작 - ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
        
        // Soft delete (활성화 상태를 false로 변경)
        user.setIsActive(false);
        userRepository.save(user);
        
        // Hard delete가 필요한 경우 (주의: 관련 데이터도 함께 삭제됨)
        // userRepository.delete(user);
        
        log.info("사용자 삭제 완료 - ID: {}", userId);
    }

    // ================================
    // 역할 관리
    // ================================

    /**
     * 사용자 역할 추가
     * 
     * 특정 사용자에게 새로운 역할을 추가합니다.
     * 
     * @param userId 사용자 ID
     * @param roleName 추가할 역할명
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     * @throws RuntimeException 역할을 찾을 수 없는 경우
     */
    @Transactional
    public void addRoleToUser(Long userId, String roleName) {
        log.info("사용자 역할 추가 - 사용자 ID: {}, 역할: {}", userId, roleName);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
        
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("역할을 찾을 수 없습니다: " + roleName));
        
        user.addRole(role);
        userRepository.save(user);
        
        log.info("사용자 역할 추가 완료 - 사용자 ID: {}, 역할: {}", userId, roleName);
    }

    /**
     * 사용자 역할 제거
     * 
     * 특정 사용자에서 역할을 제거합니다.
     * 
     * @param userId 사용자 ID
     * @param roleName 제거할 역할명
     */
    @Transactional
    public void removeRoleFromUser(Long userId, String roleName) {
        log.info("사용자 역할 제거 - 사용자 ID: {}, 역할: {}", userId, roleName);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
        
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("역할을 찾을 수 없습니다: " + roleName));
        
        user.removeRole(role);
        userRepository.save(user);
        
        log.info("사용자 역할 제거 완료 - 사용자 ID: {}, 역할: {}", userId, roleName);
    }

    // ================================
    // 통계 및 유틸리티 메서드
    // ================================

    /**
     * 전체 사용자 수 조회
     * 
     * @return 전체 사용자 수
     */
    public long getTotalUserCount() {
        return userRepository.count();
    }

    /**
     * 활성화된 사용자 수 조회
     * 
     * @return 활성화된 사용자 수
     */
    public long getActiveUserCount() {
        return userRepository.countActiveUsers();
    }

    /**
     * 최근 가입자 수 조회
     * 
     * @param days 조회할 일수
     * @return 최근 가입자 수
     */
    public long getRecentUserCount(int days) {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(days);
        LocalDateTime toDate = LocalDateTime.now();
        return userRepository.countUsersByDateRange(fromDate, toDate);
    }

    // ================================
    // 내부 메서드 (Private Methods)
    // ================================

    /**
     * 사용자명과 이메일 중복 검사
     * 
     * @param username 검사할 사용자명
     * @param email 검사할 이메일
     * @throws DuplicateUsernameException 사용자명이 중복된 경우
     * @throws DuplicateEmailException 이메일이 중복된 경우
     */
    private void validateUserUniqueness(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            log.warn("중복된 사용자명 - {}", username);
            throw new DuplicateUsernameException("이미 사용 중인 사용자명입니다: " + username);
        }
        
        if (userRepository.existsByEmail(email)) {
            log.warn("중복된 이메일 - {}", email);
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다: " + email);
        }
    }

    /**
     * User 엔티티를 UserResponse DTO로 변환
     * 
     * @param user 변환할 User 엔티티
     * @return UserResponse DTO
     */
    private UserResponse convertToUserResponse(User user) {
        UserResponse response = modelMapper.map(user, UserResponse.class);
        
        // 역할 정보를 문자열 리스트로 변환
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        response.setRoles(roleNames);
        
        return response;
    }
}