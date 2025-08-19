package com.blogapp.repository;

import com.blogapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 Repository (JPA 방식)
 * 
 * 사용자 엔티티에 대한 데이터 액세스를 담당하는 JPA Repository입니다.
 * Spring Data JPA의 기본 CRUD 기능과 커스텀 쿼리 메서드를 제공합니다.
 * 
 * 주요 기능:
 * - 기본 CRUD 연산 (JpaRepository 상속)
 * - 사용자명/이메일 기반 조회
 * - 역할 정보와 함께 조회 (N+1 문제 해결)
 * - 활성화 상태별 조회
 * - 페이징 및 정렬 지원
 * - 통계 정보 조회
 * 
 * 설계 특징:
 * - @Query 어노테이션으로 커스텀 JPQL 쿼리 정의
 * - Fetch Join으로 N+1 문제 해결
 * - 네이티브 쿼리와 JPQL 쿼리 혼용
 * - 성능 최적화를 위한 인덱스 활용
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-01-01
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ================================
    // 기본 조회 메서드
    // ================================

    /**
     * 사용자명으로 사용자 조회
     * 
     * 로그인 시 사용자 인증을 위해 사용됩니다.
     * Spring Security UserDetailsService에서 호출됩니다.
     * 
     * @param username 조회할 사용자명
     * @return 사용자 정보 (Optional)
     */
    Optional<User> findByUsername(String username);

    /**
     * 이메일로 사용자 조회
     * 
     * 비밀번호 찾기나 이메일 인증 시 사용됩니다.
     * 
     * @param email 조회할 이메일 주소
     * @return 사용자 정보 (Optional)
     */
    Optional<User> findByEmail(String email);

    /**
     * 사용자명과 역할 정보 함께 조회
     * 
     * N+1 문제를 방지하기 위해 Fetch Join을 사용합니다.
     * 사용자 인증 시 역할 정보가 필요한 경우 사용됩니다.
     * 
     * 성능 최적화:
     * - LEFT JOIN FETCH로 역할 정보 즉시 로딩
     * - 한 번의 쿼리로 사용자와 역할 정보 모두 조회
     * 
     * @param username 조회할 사용자명
     * @return 역할 정보가 포함된 사용자 정보 (Optional)
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);

    /**
     * 사용자명과 활성화 상태로 조회
     * 
     * 활성화된 사용자만 조회할 때 사용됩니다.
     * 비활성화된 계정의 로그인을 차단하는 용도로 활용됩니다.
     * 
     * @param username 조회할 사용자명
     * @return 활성화된 사용자 정보 (Optional)
     */
    Optional<User> findByUsernameAndIsActiveTrue(String username);

    // ================================
    // 존재 여부 확인 메서드
    // ================================

    /**
     * 사용자명 존재 여부 확인
     * 
     * 회원가입 시 사용자명 중복 검사에 사용됩니다.
     * 
     * @param username 확인할 사용자명
     * @return 존재하면 true, 없으면 false
     */
    boolean existsByUsername(String username);

    /**
     * 이메일 존재 여부 확인
     * 
     * 회원가입 시 이메일 중복 검사에 사용됩니다.
     * 
     * @param email 확인할 이메일 주소
     * @return 존재하면 true, 없으면 false
     */
    boolean existsByEmail(String email);

    // ================================
    // 목록 조회 메서드
    // ================================

    /**
     * 활성화된 사용자 목록 조회 (페이징)
     * 
     * 관리자 페이지에서 사용자 목록을 조회할 때 사용됩니다.
     * 페이징과 정렬을 지원합니다.
     * 
     * @param pageable 페이징 정보 (페이지 번호, 크기, 정렬)
     * @return 활성화된 사용자 목록 (페이징 처리됨)
     */
    Page<User> findByIsActiveTrue(Pageable pageable);

    /**
     * 특정 역할을 가진 사용자 목록 조회
     * 
     * 관리자 목록이나 일반 사용자 목록을 조회할 때 사용됩니다.
     * 
     * @param roleName 조회할 역할명 (예: "ADMIN", "USER")
     * @param pageable 페이징 정보
     * @return 해당 역할을 가진 사용자 목록
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Page<User> findByRoleName(@Param("roleName") String roleName, Pageable pageable);

    /**
     * 최근 가입한 사용자 목록 조회
     * 
     * 관리자 대시보드에서 최근 가입자를 확인할 때 사용됩니다.
     * 
     * @param fromDate 조회 시작 날짜
     * @param pageable 페이징 정보
     * @return 최근 가입한 사용자 목록
     */
    @Query("SELECT u FROM User u WHERE u.createdAt >= :fromDate ORDER BY u.createdAt DESC")
    Page<User> findRecentUsers(@Param("fromDate") LocalDateTime fromDate, Pageable pageable);

    // ================================
    // 검색 메서드
    // ================================

    /**
     * 사용자명 또는 이메일로 검색
     * 
     * 관리자 페이지에서 사용자 검색 기능에 사용됩니다.
     * LIKE 검색으로 부분 일치를 지원합니다.
     * 
     * @param searchTerm 검색어
     * @param pageable 페이징 정보
     * @return 검색 결과 (페이징 처리됨)
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:searchTerm% OR u.email LIKE %:searchTerm%")
    Page<User> searchByUsernameOrEmail(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * 이름으로 사용자 검색
     * 
     * 성, 이름으로 사용자를 검색할 때 사용됩니다.
     * 
     * @param firstName 이름
     * @param lastName 성
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    @Query("SELECT u FROM User u WHERE u.firstName LIKE %:firstName% AND u.lastName LIKE %:lastName%")
    Page<User> findByFirstNameContainingAndLastNameContaining(
            @Param("firstName") String firstName, 
            @Param("lastName") String lastName, 
            Pageable pageable);

    // ================================
    // 업데이트 메서드
    // ================================

    /**
     * 사용자 활성화 상태 변경
     * 
     * 관리자가 사용자 계정을 활성화/비활성화할 때 사용됩니다.
     * @Modifying 어노테이션으로 수정 쿼리임을 명시합니다.
     * 
     * @param userId 사용자 ID
     * @param isActive 활성화 상태
     * @return 변경된 레코드 수
     */
    @Modifying
    @Query("UPDATE User u SET u.isActive = :isActive, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :userId")
    int updateUserActiveStatus(@Param("userId") Long userId, @Param("isActive") Boolean isActive);

    /**
     * 마지막 로그인 시간 업데이트
     * 
     * 사용자 로그인 시 마지막 로그인 시간을 기록할 때 사용됩니다.
     * (User 엔티티에 lastLoginAt 필드가 추가된 경우)
     * 
     * @param userId 사용자 ID
     * @param lastLoginAt 마지막 로그인 시간
     * @return 변경된 레코드 수
     */
    @Modifying
    @Query("UPDATE User u SET u.updatedAt = :lastLoginAt WHERE u.id = :userId")
    int updateLastLoginTime(@Param("userId") Long userId, @Param("lastLoginAt") LocalDateTime lastLoginAt);

    // ================================
    // 통계 조회 메서드
    // ================================

    /**
     * 활성화된 사용자 수 조회
     * 
     * 시스템 통계나 대시보드에서 사용됩니다.
     * 
     * @return 활성화된 사용자 총 수
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    long countActiveUsers();

    /**
     * 특정 기간 내 가입한 사용자 수 조회
     * 
     * 가입자 통계를 확인할 때 사용됩니다.
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 해당 기간 가입자 수
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    long countUsersByDateRange(@Param("startDate") LocalDateTime startDate, 
                              @Param("endDate") LocalDateTime endDate);

    /**
     * 역할별 사용자 수 조회
     * 
     * 관리자, 일반 사용자 등 역할별 통계를 확인할 때 사용됩니다.
     * 
     * @param roleName 역할명
     * @return 해당 역할을 가진 사용자 수
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    long countUsersByRole(@Param("roleName") String roleName);

    // ================================
    // 네이티브 쿼리 메서드
    // ================================

    /**
     * 복잡한 사용자 통계 조회 (네이티브 쿼리)
     * 
     * 복잡한 집계 쿼리가 필요한 경우 네이티브 SQL을 사용합니다.
     * 
     * @return 월별 가입자 수 통계
     */
    @Query(value = """
        SELECT 
            DATE_TRUNC('month', created_at) as month,
            COUNT(*) as user_count
        FROM users 
        WHERE created_at >= CURRENT_DATE - INTERVAL '12 months'
        GROUP BY DATE_TRUNC('month', created_at)
        ORDER BY month
        """, nativeQuery = true)
    List<Object[]> getMonthlyUserRegistrationStats();

    /**
     * 사용자 활동 통계 조회
     * 
     * 사용자별 게시글 수, 파일 업로드 수 등을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 사용자 활동 통계 [게시글 수, 파일 수]
     */
    @Query(value = """
        SELECT 
            u.id,
            u.username,
            COUNT(DISTINCT p.id) as post_count,
            COUNT(DISTINCT f.id) as file_count
        FROM users u
        LEFT JOIN posts p ON u.id = p.author_id
        LEFT JOIN files f ON u.id = f.uploaded_by
        WHERE u.id = :userId
        GROUP BY u.id, u.username
        """, nativeQuery = true)
    Object[] getUserActivityStats(@Param("userId") Long userId);
}