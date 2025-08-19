package com.blogapp.repository;

import com.blogapp.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 역할(Role) Repository 인터페이스
 * 
 * 역할 정보에 대한 데이터 액세스를 담당하는 Repository입니다.
 * Spring Data JPA를 사용하여 기본적인 CRUD 작업과 커스텀 쿼리를 제공합니다.
 * 
 * 주요 기능:
 * - 역할명으로 역할 조회
 * - 활성화된 역할 목록 조회
 * - 사용자별 역할 통계
 * - 역할 권한 관리
 * 
 * 설계 원칙:
 * - Repository 패턴을 통한 데이터 접근 추상화
 * - 명명 규칙을 통한 자동 쿼리 생성
 * - @Query를 통한 복잡한 쿼리 처리
 * - 트랜잭션과 캐싱 고려
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-01-01
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * 역할명으로 역할 조회
     * 
     * 주어진 역할명에 해당하는 역할을 조회합니다.
     * 역할명은 시스템에서 유일하므로 Optional로 반환됩니다.
     * 
     * @param name 조회할 역할명 (예: "USER", "ADMIN", "MODERATOR")
     * @return 역할 정보 (Optional)
     */
    Optional<Role> findByName(String name);
    
    /**
     * 역할명 존재 여부 확인
     * 
     * 주어진 역할명이 이미 시스템에 존재하는지 확인합니다.
     * 역할 생성 시 중복 검사에 사용됩니다.
     * 
     * @param name 확인할 역할명
     * @return 존재 여부
     */
    boolean existsByName(String name);
    
    /**
     * 활성화된 역할 목록 조회
     * 
     * 시스템에서 현재 활성화되어 있는 역할들을 조회합니다.
     * 비활성화된 역할은 새로운 사용자에게 할당되지 않습니다.
     * 
     * @return 활성화된 역할 목록
     */
    @Query("SELECT r FROM Role r WHERE r.isActive = true ORDER BY r.name ASC")
    List<Role> findActiveRoles();
    
    /**
     * 기본 역할 목록 조회
     * 
     * 새로운 사용자에게 기본적으로 할당되는 역할들을 조회합니다.
     * 일반적으로 "USER" 역할이 기본 역할로 설정됩니다.
     * 
     * @return 기본 역할 목록
     */
    @Query("SELECT r FROM Role r WHERE r.isDefault = true")
    List<Role> findDefaultRoles();
    
    /**
     * 역할별 사용자 수 조회
     * 
     * 각 역할을 가진 사용자의 수를 조회합니다.
     * 관리자 대시보드에서 통계 정보로 활용됩니다.
     * 
     * @return 역할별 사용자 수 (Object 배열: [역할명, 사용자수])
     */
    @Query("SELECT r.name, COUNT(u) FROM Role r " +
           "LEFT JOIN r.users u " +
           "GROUP BY r.id, r.name " +
           "ORDER BY r.name")
    List<Object[]> findRoleUserCounts();
    
    /**
     * 관리자 역할 조회
     * 
     * 시스템 관리자 권한을 가진 역할들을 조회합니다.
     * 보안 관련 작업에서 관리자 권한 확인에 사용됩니다.
     * 
     * @return 관리자 역할 목록
     */
    @Query("SELECT r FROM Role r WHERE r.name IN ('ADMIN', 'SUPER_ADMIN') AND r.isActive = true")
    List<Role> findAdminRoles();
}