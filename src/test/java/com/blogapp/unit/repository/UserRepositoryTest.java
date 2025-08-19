package com.blogapp.unit.repository;

import com.blogapp.entity.Role;
import com.blogapp.entity.User;
import com.blogapp.repository.RoleRepository;
import com.blogapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * UserRepository 단위 테스트
 * 
 * JPA Repository의 데이터 접근 로직을 테스트합니다.
 * @DataJpaTest를 사용하여 JPA 레이어만 테스트하고 실제 데이터베이스 동작을 검증합니다.
 * 
 * 테스트 환경:
 * - H2 인메모리 데이터베이스 사용
 * - TestEntityManager로 테스트 데이터 관리
 * - 트랜잭션 자동 롤백으로 테스트 격리
 * - JPA 관련 설정만 로드
 * 
 * 테스트 범위:
 * - 기본 CRUD 작업
 * - 커스텀 쿼리 메서드
 * - 페이징 및 정렬
 * - 엔티티 관계 및 지연 로딩
 * - 데이터베이스 제약 조건
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-01-01
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository 단위 테스트")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    private User testUser;
    private Role userRole;
    private Role adminRole;
    
    @BeforeEach
    void setUp() {
        setupTestData();
    }
    
    /**
     * 테스트 데이터 초기화
     */
    private void setupTestData() {
        // 역할 생성
        userRole = Role.builder()
                .name("USER")
                .description("일반 사용자")
                .isActive(true)
                .isDefault(true)
                .build();
        entityManager.persistAndFlush(userRole);
        
        adminRole = Role.builder()
                .name("ADMIN")
                .description("관리자")
                .isActive(true)
                .isDefault(false)
                .build();
        entityManager.persistAndFlush(adminRole);
        
        // 테스트 사용자 생성
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encoded_password")
                .firstName("Test")
                .lastName("User")
                .isActive(true)
                .build();
        testUser.addRole(userRole);
        entityManager.persistAndFlush(testUser);
        
        entityManager.clear();
    }
    
    @Test
    @DisplayName("사용자명으로 사용자 조회")
    void findByUsername_Success() {
        // When
        Optional<User> result = userRepository.findByUsername("testuser");
        
        // Then
        assertThat(result).isPresent();
        User foundUser = result.get();
        assertThat(foundUser.getUsername()).isEqualTo("testuser");
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.getFirstName()).isEqualTo("Test");
        assertThat(foundUser.getLastName()).isEqualTo("User");
    }
    
    @Test
    @DisplayName("존재하지 않는 사용자명으로 조회")
    void findByUsername_NotFound() {
        // When
        Optional<User> result = userRepository.findByUsername("nonexistent");
        
        // Then
        assertThat(result).isEmpty();
    }
    
    @Test
    @DisplayName("이메일로 사용자 조회")
    void findByEmail_Success() {
        // When
        Optional<User> result = userRepository.findByEmail("test@example.com");
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }
    
    @Test
    @DisplayName("사용자명 존재 여부 확인")
    void existsByUsername_True() {
        // When
        boolean exists = userRepository.existsByUsername("testuser");
        
        // Then
        assertThat(exists).isTrue();
    }
    
    @Test
    @DisplayName("사용자명 존재 여부 확인 - 존재하지 않음")
    void existsByUsername_False() {
        // When
        boolean exists = userRepository.existsByUsername("nonexistent");
        
        // Then
        assertThat(exists).isFalse();
    }
    
    @Test
    @DisplayName("이메일 존재 여부 확인")
    void existsByEmail_True() {
        // When
        boolean exists = userRepository.existsByEmail("test@example.com");
        
        // Then
        assertThat(exists).isTrue();
    }
    
    @Test
    @DisplayName("활성화된 사용자 목록 조회")
    void findByIsActiveTrue() {
        // Given - 비활성화된 사용자 추가
        User inactiveUser = User.builder()
                .username("inactive")
                .email("inactive@example.com")
                .password("password")
                .firstName("Inactive")
                .lastName("User")
                .isActive(false)
                .build();
        entityManager.persistAndFlush(inactiveUser);
        
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<User> result = userRepository.findByIsActiveTrue(pageable);
        
        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("testuser");
        assertThat(result.getContent().get(0).getIsActive()).isTrue();
    }
    
    @Test
    @DisplayName("사용자명 또는 이메일로 검색")
    void searchByUsernameOrEmail() {
        // Given - 추가 사용자 생성
        User secondUser = User.builder()
                .username("seconduser")
                .email("second@example.com")
                .password("password")
                .firstName("Second")
                .lastName("User")
                .isActive(true)
                .build();
        entityManager.persistAndFlush(secondUser);
        
        Pageable pageable = PageRequest.of(0, 10);
        
        // When - 사용자명으로 검색
        Page<User> result1 = userRepository.searchByUsernameOrEmail("test", pageable);
        
        // Then
        assertThat(result1.getContent()).hasSize(1);
        assertThat(result1.getContent().get(0).getUsername()).isEqualTo("testuser");
        
        // When - 이메일로 검색
        Page<User> result2 = userRepository.searchByUsernameOrEmail("second@", pageable);
        
        // Then
        assertThat(result2.getContent()).hasSize(1);
        assertThat(result2.getContent().get(0).getEmail()).isEqualTo("second@example.com");
    }
    
    @Test
    @DisplayName("활성화된 사용자 수 조회")
    void countActiveUsers() {
        // Given - 비활성화된 사용자 추가
        User inactiveUser = User.builder()
                .username("inactive")
                .email("inactive@example.com")
                .password("password")
                .firstName("Inactive")
                .lastName("User")
                .isActive(false)
                .build();
        entityManager.persistAndFlush(inactiveUser);
        
        // When
        Long count = userRepository.countActiveUsers();
        
        // Then
        assertThat(count).isEqualTo(1);
    }
    
    @Test
    @DisplayName("날짜 범위별 사용자 수 조회")
    void countUsersByDateRange() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneDayAgo = now.minusDays(1);
        LocalDateTime oneHourAgo = now.minusHours(1);
        
        // When
        Long count = userRepository.countUsersByDateRange(oneDayAgo, now);
        
        // Then
        assertThat(count).isEqualTo(1); // testUser가 포함되어야 함
    }
    
    @Test
    @DisplayName("역할과 함께 사용자 조회 (Fetch Join)")
    void findByUsernameWithRoles() {
        // When
        Optional<User> result = userRepository.findByUsernameWithRoles("testuser");
        
        // Then
        assertThat(result).isPresent();
        User user = result.get();
        assertThat(user.getRoles()).isNotEmpty();
        assertThat(user.getRoles()).hasSize(1);
        assertThat(user.getRoles().iterator().next().getName()).isEqualTo("USER");
    }
    
    @Test
    @DisplayName("특정 역할을 가진 사용자 목록 조회")
    void findByRolesName() {
        // Given - 관리자 역할을 가진 사용자 추가
        User adminUser = User.builder()
                .username("admin")
                .email("admin@example.com")
                .password("password")
                .firstName("Admin")
                .lastName("User")
                .isActive(true)
                .build();
        adminUser.addRole(adminRole);
        entityManager.persistAndFlush(adminUser);
        
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<User> userRoleUsers = userRepository.findByRolesName("USER", pageable);
        Page<User> adminRoleUsers = userRepository.findByRolesName("ADMIN", pageable);
        
        // Then
        assertThat(userRoleUsers.getContent()).hasSize(1);
        assertThat(userRoleUsers.getContent().get(0).getUsername()).isEqualTo("testuser");
        
        assertThat(adminRoleUsers.getContent()).hasSize(1);
        assertThat(adminRoleUsers.getContent().get(0).getUsername()).isEqualTo("admin");
    }
    
    @Test
    @DisplayName("사용자 저장 및 조회")
    void saveAndFind() {
        // Given
        User newUser = User.builder()
                .username("newuser")
                .email("new@example.com")
                .password("password")
                .firstName("New")
                .lastName("User")
                .isActive(true)
                .build();
        newUser.addRole(userRole);
        
        // When
        User savedUser = userRepository.save(newUser);
        entityManager.flush();
        entityManager.clear();
        
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        
        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("newuser");
        assertThat(foundUser.get().getEmail()).isEqualTo("new@example.com");
        assertThat(foundUser.get().getRoles()).hasSize(1);
    }
    
    @Test
    @DisplayName("사용자 삭제")
    void deleteUser() {
        // Given
        Long userId = testUser.getId();
        
        // When
        userRepository.deleteById(userId);
        entityManager.flush();
        
        // Then
        Optional<User> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isEmpty();
    }
}