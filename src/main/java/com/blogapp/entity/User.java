package com.blogapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 사용자 엔티티
 * 
 * 이 클래스는 시스템의 사용자 정보를 나타내는 JPA 엔티티입니다.
 * 
 * 주요 기능:
 * - 사용자 기본 정보 저장 (아이디, 이메일, 비밀번호, 이름)
 * - 역할(Role) 다대다 관계 매핑
 * - 게시글(Post) 일대다 관계 매핑
 * - 업로드한 파일(File) 일대다 관계 매핑
 * - 계정 활성화 상태 관리
 * - 생성/수정 시간 자동 추적
 * 
 * 설계 특징:
 * - @Table 어노테이션으로 테이블명과 유니크 제약조건 명시
 * - Lombok을 활용한 boilerplate 코드 최소화
 * - 양방향 관계에서 연관관계 편의 메서드 제공
 * - 무한루프 방지를 위한 toString exclude 설정
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-01-01
 */
@Entity
@Table(name = "users", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "username"),
           @UniqueConstraint(columnNames = "email")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"roles", "posts", "files", "password"})
@EqualsAndHashCode(of = "id")
public class User {

    /**
     * 사용자 고유 식별자
     * 
     * PostgreSQL의 BIGSERIAL 타입을 사용하여 자동 증가되는 기본키입니다.
     * GenerationType.IDENTITY를 사용하여 데이터베이스에서 자동 생성됩니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자명 (로그인 아이디)
     * 
     * 시스템 내에서 유일해야 하며, 로그인 시 식별자로 사용됩니다.
     * 50자까지 입력 가능하며 NULL 값을 허용하지 않습니다.
     */
    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    /**
     * 이메일 주소
     * 
     * 사용자의 이메일 주소로 시스템 내에서 유일해야 합니다.
     * 100자까지 입력 가능하며 계정 복구나 알림 발송에 사용됩니다.
     */
    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    /**
     * 암호화된 비밀번호
     * 
     * BCrypt 등의 해시 알고리즘으로 암호화되어 저장됩니다.
     * 255자까지 저장 가능하여 다양한 해시 알고리즘을 수용할 수 있습니다.
     */
    @Column(name = "password", length = 255, nullable = false)
    private String password;

    /**
     * 이름 (성)
     * 
     * 사용자의 성을 저장합니다.
     * 50자까지 입력 가능합니다.
     */
    @Column(name = "first_name", length = 50, nullable = false)
    private String firstName;

    /**
     * 이름 (이름)
     * 
     * 사용자의 이름을 저장합니다.
     * 50자까지 입력 가능합니다.
     */
    @Column(name = "last_name", length = 50, nullable = false)
    private String lastName;

    /**
     * 계정 활성화 상태
     * 
     * 계정의 활성화 여부를 나타냅니다.
     * true: 활성화된 계정 (로그인 가능)
     * false: 비활성화된 계정 (로그인 불가)
     * 기본값은 true입니다.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 계정 생성 시간
     * 
     * 레코드가 생성될 때 자동으로 현재 시간이 설정됩니다.
     * @CreationTimestamp 어노테이션으로 Hibernate가 자동 관리합니다.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 계정 최종 수정 시간
     * 
     * 레코드가 수정될 때마다 자동으로 현재 시간으로 업데이트됩니다.
     * @UpdateTimestamp 어노테이션으로 Hibernate가 자동 관리합니다.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 사용자 역할 목록
     * 
     * 사용자가 가진 역할들을 나타내는 다대다 관계입니다.
     * - 한 사용자는 여러 역할을 가질 수 있습니다 (예: USER + ADMIN)
     * - @JoinTable로 중간 테이블(user_roles) 설정
     * - FetchType.EAGER로 사용자 조회 시 역할도 함께 로드
     * - CascadeType.MERGE로 역할 변경 시 함께 저장
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /**
     * 사용자가 작성한 게시글 목록
     * 
     * 사용자와 게시글 간의 일대다 관계입니다.
     * - 한 사용자는 여러 게시글을 작성할 수 있습니다
     * - FetchType.LAZY로 지연 로딩 (필요할 때만 조회)
     * - CascadeType.ALL로 사용자 삭제 시 게시글도 함께 삭제
     * - orphanRemoval로 관계가 끊어진 게시글 자동 삭제
     */
    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY, 
               cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Post> posts = new HashSet<>();

    /**
     * 사용자가 업로드한 파일 목록
     * 
     * 사용자와 파일 간의 일대다 관계입니다.
     * - 한 사용자는 여러 파일을 업로드할 수 있습니다
     * - FetchType.LAZY로 지연 로딩
     * - CascadeType.ALL로 사용자 삭제 시 파일 정보도 함께 삭제
     */
    @OneToMany(mappedBy = "uploadedBy", fetch = FetchType.LAZY, 
               cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<FileEntity> files = new HashSet<>();

    /**
     * 역할 추가 편의 메서드
     * 
     * 사용자에게 새로운 역할을 추가합니다.
     * 양방향 관계를 정확히 설정하기 위해 양쪽 모두에 관계를 설정합니다.
     * 
     * @param role 추가할 역할
     */
    public void addRole(Role role) {
        this.roles.add(role);
        role.getUsers().add(this);
    }

    /**
     * 역할 제거 편의 메서드
     * 
     * 사용자에서 특정 역할을 제거합니다.
     * 양방향 관계를 정확히 해제하기 위해 양쪽 모두에서 관계를 제거합니다.
     * 
     * @param role 제거할 역할
     */
    public void removeRole(Role role) {
        this.roles.remove(role);
        role.getUsers().remove(this);
    }

    /**
     * 게시글 추가 편의 메서드
     * 
     * 사용자에게 새로운 게시글을 추가합니다.
     * 양방향 관계를 정확히 설정하기 위해 게시글의 작성자도 설정합니다.
     * 
     * @param post 추가할 게시글
     */
    public void addPost(Post post) {
        this.posts.add(post);
        post.setAuthor(this);
    }

    /**
     * 게시글 제거 편의 메서드
     * 
     * 사용자에서 특정 게시글을 제거합니다.
     * 양방향 관계를 정확히 해제하기 위해 게시글의 작성자도 null로 설정합니다.
     * 
     * @param post 제거할 게시글
     */
    public void removePost(Post post) {
        this.posts.remove(post);
        post.setAuthor(null);
    }

    /**
     * 사용자의 전체 이름 반환
     * 
     * firstName과 lastName을 조합하여 전체 이름을 반환합니다.
     * 
     * @return 전체 이름 (예: "홍 길동")
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * 특정 역할 보유 여부 확인
     * 
     * 사용자가 특정 역할을 가지고 있는지 확인합니다.
     * 
     * @param roleName 확인할 역할명 (예: "ADMIN", "USER")
     * @return 역할 보유 여부
     */
    public boolean hasRole(String roleName) {
        return roles.stream()
                   .anyMatch(role -> role.getName().equals(roleName));
    }

    /**
     * 관리자 권한 보유 여부 확인
     * 
     * 사용자가 관리자 권한을 가지고 있는지 확인합니다.
     * 
     * @return 관리자 권한 보유 여부
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }
}