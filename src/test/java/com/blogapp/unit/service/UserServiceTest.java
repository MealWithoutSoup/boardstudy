package com.blogapp.unit.service;

import com.blogapp.dto.request.UserRegistrationRequest;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserService 단위 테스트
 * 
 * 사용자 서비스의 비즈니스 로직을 단위 테스트로 검증합니다.
 * Mockito를 활용하여 의존성을 모킹하고 격리된 테스트를 수행합니다.
 * 
 * 테스트 범위:
 * - 회원가입 로직 검증
 * - 중복 검사 로직 검증
 * - 예외 처리 검증
 * - 비즈니스 규칙 검증
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private RoleRepository roleRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private ModelMapper modelMapper;
    
    @InjectMocks
    private UserService userService;
    
    private UserRegistrationRequest validRequest;
    private Role userRole;
    private User savedUser;
    
    @BeforeEach
    void setUp() {
        // 테스트 데이터 준비
        validRequest = new UserRegistrationRequest();
        validRequest.setUsername("testuser");
        validRequest.setEmail("test@example.com");
        validRequest.setPassword("password123");
        validRequest.setFirstName("Test");
        validRequest.setLastName("User");
        
        userRole = Role.builder()
                .id(1L)
                .name("USER")
                .description("일반 사용자")
                .build();
                
        savedUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encoded_password")
                .firstName("Test")
                .lastName("User")
                .isActive(true)
                .build();
    }
    
    @Test
    @DisplayName("회원가입 성공")
    void registerUser_Success() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // When
        var result = userService.registerUser(validRequest);
        
        // Then
        assertNotNull(result);
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(roleRepository).findByName("USER");
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    @DisplayName("중복 사용자명으로 회원가입 실패")
    void registerUser_DuplicateUsername() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        
        // When & Then
        assertThrows(DuplicateUsernameException.class, 
                    () -> userService.registerUser(validRequest));
        
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    @DisplayName("중복 이메일로 회원가입 실패")
    void registerUser_DuplicateEmail() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        
        // When & Then
        assertThrows(DuplicateEmailException.class, 
                    () -> userService.registerUser(validRequest));
        
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }
}