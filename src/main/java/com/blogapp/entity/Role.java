package com.blogapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * 역할 엔티티
 * 
 * 이 클래스는 시스템의 사용자 역할을 나타내는 JPA 엔티티입니다.
 * 
 * 주요 기능:
 * - 역할 정보 저장 (이름, 설명)
 * - 사용자(User)와 다대다 관계 매핑
 * - 권한 기반 접근 제어를 위한 역할 관리
 * 
 * 기본 역할:
 * - ADMIN: 시스템 관리자 (모든 권한)
 * - USER: 일반 사용자 (기본 권한)
 * 
 * 설계 특징:
 * - 역할명은 시스템 내에서 유일해야 함
 * - 사용자와의 다대다 관계에서 주인이 아닌 쪽
 * - 역할 추가/제거 시 관련 사용자들과의 관계도 관리
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-01-01
 */
@Entity
@Table(name = "roles",
       uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "users")
@EqualsAndHashCode(of = "id")
public class Role {

    /**
     * 역할 고유 식별자
     * 
     * PostgreSQL의 BIGSERIAL 타입을 사용하여 자동 증가되는 기본키입니다.
     * GenerationType.IDENTITY를 사용하여 데이터베이스에서 자동 생성됩니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 역할명
     * 
     * 시스템에서 사용하는 역할의 이름입니다.
     * 예: "ADMIN", "USER", "MODERATOR" 등
     * 
     * 특징:
     * - 최대 20자까지 저장 가능
     * - 시스템 내에서 유일해야 함
     * - 일반적으로 대문자로 표기
     * - Spring Security와 연동하여 권한 체크에 사용
     */
    @Column(name = "name", length = 20, nullable = false, unique = true)
    private String name;

    /**
     * 역할 설명
     * 
     * 해당 역할에 대한 상세 설명입니다.
     * 관리자나 개발자가 역할의 목적을 이해하기 위해 사용됩니다.
     * 
     * 예:
     * - "시스템 관리자" (ADMIN 역할)
     * - "일반 사용자" (USER 역할)
     * - "컨텐츠 운영자" (MODERATOR 역할)
     */
    @Column(name = "description", length = 100)
    private String description;

    /**
     * 이 역할을 가진 사용자 목록
     * 
     * 사용자와 역할 간의 다대다 관계입니다.
     * - 한 역할은 여러 사용자가 가질 수 있습니다
     * - mappedBy로 User 엔티티의 roles 필드가 관계의 주인임을 명시
     * - FetchType.LAZY로 지연 로딩 (필요할 때만 사용자 목록 조회)
     * - CascadeType.MERGE로 역할 변경 시 관련 사용자도 함께 저장
     */
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @Builder.Default
    private Set<User> users = new HashSet<>();

    /**
     * 사용자 추가 편의 메서드
     * 
     * 이 역할에 새로운 사용자를 추가합니다.
     * 양방향 관계를 정확히 설정하기 위해 사용자의 역할 목록에도 추가합니다.
     * 
     * 사용 예시:
     * Role adminRole = roleRepository.findByName("ADMIN");
     * User newAdmin = userRepository.findById(userId);
     * adminRole.addUser(newAdmin);
     * 
     * @param user 이 역할을 부여받을 사용자
     */
    public void addUser(User user) {
        this.users.add(user);
        user.getRoles().add(this);
    }

    /**
     * 사용자 제거 편의 메서드
     * 
     * 이 역할에서 특정 사용자를 제거합니다.
     * 양방향 관계를 정확히 해제하기 위해 사용자의 역할 목록에서도 제거합니다.
     * 
     * 사용 예시:
     * Role adminRole = roleRepository.findByName("ADMIN");
     * User formerAdmin = userRepository.findById(userId);
     * adminRole.removeUser(formerAdmin);
     * 
     * @param user 이 역할을 제거할 사용자
     */
    public void removeUser(User user) {
        this.users.remove(user);
        user.getRoles().remove(this);
    }

    /**
     * 역할이 비어있는지 확인
     * 
     * 이 역할을 가진 사용자가 있는지 확인합니다.
     * 역할 삭제 전에 확인용으로 사용할 수 있습니다.
     * 
     * @return 역할을 가진 사용자가 없으면 true, 있으면 false
     */
    public boolean isEmpty() {
        return users.isEmpty();
    }

    /**
     * 사용자 수 반환
     * 
     * 이 역할을 가진 사용자의 총 수를 반환합니다.
     * 
     * @return 이 역할을 가진 사용자 수
     */
    public int getUserCount() {
        return users.size();
    }

    /**
     * 관리자 역할 여부 확인
     * 
     * 이 역할이 관리자 역할인지 확인합니다.
     * 
     * @return 관리자 역할이면 true, 아니면 false
     */
    public boolean isAdminRole() {
        return "ADMIN".equals(this.name);
    }

    /**
     * 일반 사용자 역할 여부 확인
     * 
     * 이 역할이 일반 사용자 역할인지 확인합니다.
     * 
     * @return 일반 사용자 역할이면 true, 아니면 false
     */
    public boolean isUserRole() {
        return "USER".equals(this.name);
    }
}