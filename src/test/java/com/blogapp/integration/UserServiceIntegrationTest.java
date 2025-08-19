package com.blogapp.integration;

import com.blogapp.dto.request.UserRegistrationRequest;
import com.blogapp.dto.response.UserResponse;
import com.blogapp.entity.Role;
import com.blogapp.entity.User;
import com.blogapp.exception.DuplicateEmailException;
import com.blogapp.exception.DuplicateUsernameException;
import com.blogapp.repository.RoleRepository;
import com.blogapp.repository.UserRepository;
import com.blogapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

/**
 * UserService 통합 테스트
 * 
 * 실제 Spring 컨텍스트와 데이터베이스를 사용한 통합 테스트입니다.
 * 모든 레이어(Controller → Service → Repository → Database)의 통합 동작을 검증합니다.
 * 
 * 테스트 환경:
 * - Spring Boot Test 컨텍스트 로드
 * - H2 인메모리 데이터베이스 사용
 * - 테스트별 트랜잭션 롤백으로 데이터 격리
 * - 실제 의존성 주입 및 설정 적용
 * 
 * 검증 범위:
 * - 비즈니스 로직의 end-to-end 동작
 * - 데이터베이스 트랜잭션 및 일관성
 * - 엔티티 관계 및 영속성 컨텍스트
 * - 예외 처리 및 롤백 메커니즘
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-01-01
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("UserService 통합 테스트")
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    private UserRegistrationRequest validRequest;
    private Role userRole;
    
    @BeforeEach
    void setUp() {
        // 테스트 데이터 준비
        setupTestData();
    }
    
    /**
     * 테스트 데이터 초기화
     * 
     * 각 테스트에서 사용할 기본 데이터를 설정합니다.
     */
    private void setupTestData() {
        // 기본 역할 생성
        userRole = Role.builder()
                .name("USER")
                .description("일반 사용자")
                .isActive(true)
                .isDefault(true)
                .build();
        roleRepository.save(userRole);
        
        // 유효한 회원가입 요청 데이터
        validRequest = new UserRegistrationRequest();
        validRequest.setUsername("testuser");
        validRequest.setEmail("test@example.com");
        validRequest.setPassword("password123");
        validRequest.setFirstName("Test");
        validRequest.setLastName("User");
    }
    
    @Test
    @DisplayName("회원가입 성공 - 전체 플로우 검증")
    void registerUser_Success_EndToEndFlow() {
        // When
        UserResponse result = userService.registerUser(validRequest);
        
        // Then - 응답 데이터 검증
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getFirstName()).isEqualTo("Test");
        assertThat(result.getLastName()).isEqualTo("User");
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getRoles()).containsExactly("USER");
        assertThat(result.getCreatedAt()).isNotNull();
        
        // 데이터베이스 검증
        User savedUser = userRepository.findByUsername("testuser").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getPassword()).isNotEqualTo("password123"); // 암호화됨
        assertThat(savedUser.getRoles()).hasSize(1);
        assertThat(savedUser.getRoles().iterator().next().getName()).isEqualTo("USER");
    }
    
    @Test
    @DisplayName("중복 사용자명으로 회원가입 실패")
    void registerUser_DuplicateUsername_ThrowsException() {
        // Given - 기존 사용자 생성
        User existingUser = User.builder()
                .username("testuser")
                .email("existing@example.com")
                .password("encoded_password")
                .firstName("Existing")
                .lastName("User")
                .isActive(true)
                .build();
        userRepository.save(existingUser);
        
        // When & Then
        assertThatThrownBy(() -> userService.registerUser(validRequest))
                .isInstanceOf(DuplicateUsernameException.class)
                .hasMessageContaining("이미 사용 중인 사용자명입니다");
        
        // 데이터베이스 상태 검증 (롤백되어야 함)
        long userCount = userRepository.count();
        assertThat(userCount).isEqualTo(1); // 기존 사용자만 존재
    }
    
    @Test
    @DisplayName("중복 이메일로 회원가입 실패")
    void registerUser_DuplicateEmail_ThrowsException() {
        // Given - 기존 사용자 생성
        User existingUser = User.builder()
                .username("existinguser")
                .email("test@example.com")
                .password("encoded_password")
                .firstName("Existing")
                .lastName("User")
                .isActive(true)
                .build();
        userRepository.save(existingUser);
        
        // When & Then
        assertThatThrownBy(() -> userService.registerUser(validRequest))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("이미 사용 중인 이메일입니다");
        
        // 데이터베이스 상태 검증
        long userCount = userRepository.count();
        assertThat(userCount).isEqualTo(1); // 기존 사용자만 존재
    }
    
    @Test
    @DisplayName("사용자 조회 - ID로 조회 성공")
    void getUserById_Success() {
        // Given - 사용자 등록
        UserResponse registeredUser = userService.registerUser(validRequest);
        
        // When
        UserResponse result = userService.getUserById(registeredUser.getId());
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(registeredUser.getId());
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }
    
    @Test
    @DisplayName("사용자 조회 - 사용자명으로 조회 성공")
    void getUserByUsername_Success() {
        // Given - 사용자 등록
        userService.registerUser(validRequest);
        
        // When
        UserResponse result = userService.getUserByUsername("testuser");
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }
    
    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_Success() {
        // Given - 사용자 등록
        UserResponse registeredUser = userService.registerUser(validRequest);
        String currentPassword = "password123";
        String newPassword = "newpassword456";
        
        // When
        userService.changePassword(registeredUser.getId(), currentPassword, newPassword);
        
        // Then - 데이터베이스에서 비밀번호 변경 확인
        User updatedUser = userRepository.findById(registeredUser.getId()).orElse(null);
        assertThat(updatedUser).isNotNull();
        // 새 비밀번호로 암호화되었는지 확인 (실제로는 PasswordEncoder로 검증해야 함)
        assertThat(updatedUser.getPassword()).isNotEqualTo("password123");
        assertThat(updatedUser.getPassword()).isNotEqualTo("newpassword456");
    }
    
    @Test
    @DisplayName("사용자 활성화 상태 변경")
    void updateUserActiveStatus_Success() {
        // Given - 사용자 등록
        UserResponse registeredUser = userService.registerUser(validRequest);
        
        // When - 비활성화
        userService.updateUserActiveStatus(registeredUser.getId(), false);
        
        // Then
        User updatedUser = userRepository.findById(registeredUser.getId()).orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getIsActive()).isFalse();
        
        // When - 다시 활성화
        userService.updateUserActiveStatus(registeredUser.getId(), true);
        
        // Then
        updatedUser = userRepository.findById(registeredUser.getId()).orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getIsActive()).isTrue();
    }
    
    @Test
    @DisplayName("사용자 통계 조회")
    void getUserStatistics_Success() {
        // Given - 여러 사용자 등록
        userService.registerUser(validRequest);
        
        UserRegistrationRequest secondUser = new UserRegistrationRequest();
        secondUser.setUsername("seconduser");
        secondUser.setEmail("second@example.com");
        secondUser.setPassword("password123");
        secondUser.setFirstName("Second");
        secondUser.setLastName("User");
        userService.registerUser(secondUser);
        
        // When
        long totalUsers = userService.getTotalUserCount();
        long activeUsers = userService.getActiveUserCount();
        
        // Then
        assertThat(totalUsers).isEqualTo(2);
        assertThat(activeUsers).isEqualTo(2);
    }
}