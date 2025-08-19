package com.blogapp.repository.mybatis;

import com.blogapp.entity.User;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 MyBatis Mapper 인터페이스
 * 
 * MyBatis를 사용한 사용자 데이터 액세스를 담당하는 매퍼입니다.
 * JPA Repository와 동일한 기능을 MyBatis 방식으로 구현합니다.
 * 
 * 주요 기능:
 * - SQL 쿼리 직접 제어
 * - 복잡한 조인 쿼리 최적화
 * - 동적 쿼리 생성
 * - 배치 처리 지원
 * - 네이티브 SQL 활용
 * 
 * 설계 특징:
 * - @Mapper 어노테이션으로 MyBatis 매퍼 등록
 * - Annotation 기반 쿼리와 XML 기반 쿼리 혼용
 * - 복잡한 쿼리는 XML 파일로 분리
 * - JPA Repository와 동일한 메서드 시그니처 유지
 * 
 * 학습 목적:
 * - MyBatis와 JPA의 차이점 이해
 * - SQL 쿼리 최적화 학습
 * - 동적 쿼리 작성 방법 학습
 * - 성능 튜닝 기법 습득
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-01-01
 */
@Mapper
public interface UserMapper {

    // ================================
    // 기본 CRUD 연산
    // ================================

    /**
     * 사용자 등록
     * 
     * 새로운 사용자를 데이터베이스에 삽입합니다.
     * MyBatis에서는 @Options를 통해 자동 생성된 키를 받아올 수 있습니다.
     * 
     * @param user 등록할 사용자 정보
     * @return 삽입된 레코드 수
     */
    @Insert("""
        INSERT INTO users (username, email, password, first_name, last_name, is_active, created_at, updated_at)
        VALUES (#{username}, #{email}, #{password}, #{firstName}, #{lastName}, #{isActive}, 
                #{createdAt}, #{updatedAt})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertUser(User user);

    /**
     * 사용자 정보 수정
     * 
     * 기존 사용자의 정보를 업데이트합니다.
     * 비밀번호는 별도 메서드로 분리하여 보안성을 높입니다.
     * 
     * @param user 수정할 사용자 정보
     * @return 수정된 레코드 수
     */
    @Update("""
        UPDATE users 
        SET email = #{email}, 
            first_name = #{firstName}, 
            last_name = #{lastName}, 
            is_active = #{isActive},
            updated_at = CURRENT_TIMESTAMP
        WHERE id = #{id}
        """)
    int updateUser(User user);

    /**
     * 사용자 삭제
     * 
     * 실제로는 물리적 삭제보다는 is_active를 false로 설정하는 것을 권장합니다.
     * 
     * @param id 삭제할 사용자 ID
     * @return 삭제된 레코드 수
     */
    @Delete("DELETE FROM users WHERE id = #{id}")
    int deleteUser(@Param("id") Long id);

    /**
     * ID로 사용자 조회
     * 
     * 기본키를 사용한 단일 사용자 조회입니다.
     * 
     * @param id 사용자 ID
     * @return 사용자 정보 (Optional)
     */
    @Select("SELECT * FROM users WHERE id = #{id}")
    @Results({
        @Result(property = "firstName", column = "first_name"),
        @Result(property = "lastName", column = "last_name"),
        @Result(property = "isActive", column = "is_active"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    Optional<User> findById(@Param("id") Long id);

    // ================================
    // 조회 메서드 (Annotation 기반)
    // ================================

    /**
     * 사용자명으로 사용자 조회
     * 
     * 로그인 시 사용자 인증을 위해 사용됩니다.
     * 
     * @param username 조회할 사용자명
     * @return 사용자 정보 (Optional)
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    @Results(id = "userResultMap", value = {
        @Result(property = "firstName", column = "first_name"),
        @Result(property = "lastName", column = "last_name"),
        @Result(property = "isActive", column = "is_active"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    Optional<User> findByUsername(@Param("username") String username);

    /**
     * 이메일로 사용자 조회
     * 
     * 비밀번호 찾기나 이메일 인증 시 사용됩니다.
     * 
     * @param email 조회할 이메일 주소
     * @return 사용자 정보 (Optional)
     */
    @Select("SELECT * FROM users WHERE email = #{email}")
    @ResultMap("userResultMap")
    Optional<User> findByEmail(@Param("email") String email);

    /**
     * 활성화된 사용자만 조회
     * 
     * 비활성화된 계정의 로그인을 차단하는 용도로 활용됩니다.
     * 
     * @param username 조회할 사용자명
     * @return 활성화된 사용자 정보 (Optional)
     */
    @Select("SELECT * FROM users WHERE username = #{username} AND is_active = true")
    @ResultMap("userResultMap")
    Optional<User> findByUsernameAndIsActiveTrue(@Param("username") String username);

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
    @Select("SELECT COUNT(*) > 0 FROM users WHERE username = #{username}")
    boolean existsByUsername(@Param("username") String username);

    /**
     * 이메일 존재 여부 확인
     * 
     * 회원가입 시 이메일 중복 검사에 사용됩니다.
     * 
     * @param email 확인할 이메일 주소
     * @return 존재하면 true, 없으면 false
     */
    @Select("SELECT COUNT(*) > 0 FROM users WHERE email = #{email}")
    boolean existsByEmail(@Param("email") String email);

    // ================================
    // 목록 조회 메서드 (XML 매핑 사용)
    // ================================

    /**
     * 사용자 목록 조회 (페이징)
     * 
     * 복잡한 페이징 쿼리는 XML 파일에서 정의합니다.
     * MyBatis의 동적 쿼리 기능을 활용합니다.
     * 
     * @param offset 시작 위치
     * @param limit 조회할 개수
     * @param sortBy 정렬 기준
     * @param sortDirection 정렬 방향 (ASC/DESC)
     * @return 사용자 목록
     */
    List<User> findUsers(@Param("offset") int offset, 
                        @Param("limit") int limit,
                        @Param("sortBy") String sortBy,
                        @Param("sortDirection") String sortDirection);

    /**
     * 활성화된 사용자 목록 조회
     * 
     * 관리자 페이지에서 사용자 목록을 조회할 때 사용됩니다.
     * 
     * @param offset 시작 위치
     * @param limit 조회할 개수
     * @return 활성화된 사용자 목록
     */
    List<User> findActiveUsers(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 특정 역할을 가진 사용자 목록 조회
     * 
     * 복잡한 조인 쿼리로 XML에서 정의합니다.
     * 
     * @param roleName 조회할 역할명
     * @param offset 시작 위치
     * @param limit 조회할 개수
     * @return 해당 역할을 가진 사용자 목록
     */
    List<User> findUsersByRole(@Param("roleName") String roleName,
                              @Param("offset") int offset, 
                              @Param("limit") int limit);

    // ================================
    // 검색 메서드
    // ================================

    /**
     * 사용자 검색 (동적 쿼리)
     * 
     * 여러 조건으로 사용자를 검색할 때 사용됩니다.
     * MyBatis의 동적 쿼리 기능을 활용하여 XML에서 구현합니다.
     * 
     * @param searchParams 검색 조건들
     * @param offset 시작 위치
     * @param limit 조회할 개수
     * @return 검색 결과
     */
    List<User> searchUsers(@Param("searchParams") UserSearchParams searchParams,
                          @Param("offset") int offset,
                          @Param("limit") int limit);

    // ================================
    // 배치 처리 메서드
    // ================================

    /**
     * 여러 사용자 일괄 등록
     * 
     * 대량의 사용자 데이터를 효율적으로 삽입할 때 사용됩니다.
     * MyBatis의 foreach 기능을 활용합니다.
     * 
     * @param users 등록할 사용자 목록
     * @return 삽입된 레코드 수
     */
    int insertUsers(@Param("users") List<User> users);

    /**
     * 여러 사용자 활성화 상태 일괄 변경
     * 
     * 관리자가 여러 사용자의 상태를 한 번에 변경할 때 사용됩니다.
     * 
     * @param userIds 변경할 사용자 ID 목록
     * @param isActive 변경할 활성화 상태
     * @return 변경된 레코드 수
     */
    int updateUsersActiveStatus(@Param("userIds") List<Long> userIds, 
                               @Param("isActive") Boolean isActive);

    // ================================
    // 통계 조회 메서드
    // ================================

    /**
     * 전체 사용자 수 조회
     * 
     * @return 전체 사용자 수
     */
    @Select("SELECT COUNT(*) FROM users")
    long countAllUsers();

    /**
     * 활성화된 사용자 수 조회
     * 
     * @return 활성화된 사용자 수
     */
    @Select("SELECT COUNT(*) FROM users WHERE is_active = true")
    long countActiveUsers();

    /**
     * 특정 기간 내 가입한 사용자 수 조회
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 해당 기간 가입자 수
     */
    @Select("""
        SELECT COUNT(*) FROM users 
        WHERE created_at BETWEEN #{startDate} AND #{endDate}
        """)
    long countUsersByDateRange(@Param("startDate") LocalDateTime startDate, 
                              @Param("endDate") LocalDateTime endDate);

    // ================================
    // 복잡한 조회 메서드 (XML 매핑 사용)
    // ================================

    /**
     * 사용자와 역할 정보 함께 조회
     * 
     * N+1 문제를 방지하기 위해 조인 쿼리를 사용합니다.
     * 복잡한 매핑이므로 XML에서 정의합니다.
     * 
     * @param username 조회할 사용자명
     * @return 역할 정보가 포함된 사용자 정보
     */
    Optional<User> findByUsernameWithRoles(@Param("username") String username);

    /**
     * 사용자 활동 통계 조회
     * 
     * 사용자별 게시글 수, 파일 업로드 수 등을 조회합니다.
     * 복잡한 집계 쿼리이므로 XML에서 구현합니다.
     * 
     * @param userId 사용자 ID
     * @return 사용자 활동 통계
     */
    UserActivityStats getUserActivityStats(@Param("userId") Long userId);

    /**
     * 월별 가입자 통계 조회
     * 
     * 관리자 대시보드용 통계 데이터를 조회합니다.
     * 
     * @param months 조회할 개월 수
     * @return 월별 가입자 통계
     */
    List<MonthlyStats> getMonthlyRegistrationStats(@Param("months") int months);

    // ================================
    // 내부 클래스 (DTO)
    // ================================

    /**
     * 사용자 검색 파라미터 클래스
     * 
     * 동적 쿼리에서 사용할 검색 조건들을 담는 클래스입니다.
     */
    class UserSearchParams {
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private Boolean isActive;
        private LocalDateTime createdAfter;
        private LocalDateTime createdBefore;
        private String roleName;

        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
        
        public LocalDateTime getCreatedAfter() { return createdAfter; }
        public void setCreatedAfter(LocalDateTime createdAfter) { this.createdAfter = createdAfter; }
        
        public LocalDateTime getCreatedBefore() { return createdBefore; }
        public void setCreatedBefore(LocalDateTime createdBefore) { this.createdBefore = createdBefore; }
        
        public String getRoleName() { return roleName; }
        public void setRoleName(String roleName) { this.roleName = roleName; }
    }

    /**
     * 사용자 활동 통계 클래스
     */
    class UserActivityStats {
        private Long userId;
        private String username;
        private long postCount;
        private long fileCount;
        private LocalDateTime lastActivityAt;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public long getPostCount() { return postCount; }
        public void setPostCount(long postCount) { this.postCount = postCount; }
        
        public long getFileCount() { return fileCount; }
        public void setFileCount(long fileCount) { this.fileCount = fileCount; }
        
        public LocalDateTime getLastActivityAt() { return lastActivityAt; }
        public void setLastActivityAt(LocalDateTime lastActivityAt) { this.lastActivityAt = lastActivityAt; }
    }

    /**
     * 월별 통계 클래스
     */
    class MonthlyStats {
        private String month;
        private long userCount;

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        
        public long getUserCount() { return userCount; }
        public void setUserCount(long userCount) { this.userCount = userCount; }
    }
}