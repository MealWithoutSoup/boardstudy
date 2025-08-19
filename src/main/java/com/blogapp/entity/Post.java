package com.blogapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 게시글 엔티티
 * 
 * 이 클래스는 블로그 시스템의 게시글을 나타내는 JPA 엔티티입니다.
 * 
 * 주요 기능:
 * - 게시글 기본 정보 저장 (제목, 내용, 요약)
 * - 작성자(User)와 다대일 관계 매핑
 * - 첨부 파일(File)과 일대다 관계 매핑
 * - 게시글 발행 상태 관리
 * - 생성/수정 시간 자동 추적
 * 
 * 설계 특징:
 * - 작성자 정보는 필수 (NOT NULL)
 * - 발행 여부로 임시 저장 기능 지원
 * - 첨부 파일들과의 관계 관리
 * - 감사(Audit) 정보 자동 관리
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-01-01
 */
@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"author", "files"})
@EqualsAndHashCode(of = "id")
public class Post {

    /**
     * 게시글 고유 식별자
     * 
     * PostgreSQL의 BIGSERIAL 타입을 사용하여 자동 증가되는 기본키입니다.
     * GenerationType.IDENTITY를 사용하여 데이터베이스에서 자동 생성됩니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 게시글 제목
     * 
     * 게시글의 제목으로 사용자에게 노출되는 주요 정보입니다.
     * 
     * 특징:
     * - 최대 200자까지 저장 가능
     * - 필수 입력 항목 (NOT NULL)
     * - 검색 시 주요 대상 필드
     * - SEO를 위한 메타 정보로도 활용
     */
    @Column(name = "title", length = 200, nullable = false)
    private String title;

    /**
     * 게시글 본문 내용
     * 
     * 게시글의 실제 내용이 저장되는 필드입니다.
     * 
     * 특징:
     * - TEXT 타입으로 대용량 텍스트 저장 가능
     * - HTML 마크업 지원 (에디터에서 작성된 내용)
     * - 필수 입력 항목 (NOT NULL)
     * - 전문 검색 대상 필드
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 게시글 요약
     * 
     * 게시글의 간단한 요약 또는 미리보기 텍스트입니다.
     * 
     * 특징:
     * - 최대 500자까지 저장 가능
     * - 선택 입력 항목 (NULL 허용)
     * - 게시글 목록에서 미리보기로 사용
     * - SEO 메타 디스크립션으로 활용 가능
     */
    @Column(name = "summary", length = 500)
    private String summary;

    /**
     * 게시글 발행 상태
     * 
     * 게시글이 공개되었는지 여부를 나타냅니다.
     * 
     * 특징:
     * - true: 발행된 게시글 (공개, 목록에 노출)
     * - false: 임시 저장된 게시글 (비공개, 작성자만 조회 가능)
     * - 기본값은 false (임시 저장 상태)
     * - 관리자는 모든 게시글 조회 가능
     */
    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private Boolean isPublished = false;

    /**
     * 게시글 작성자
     * 
     * 이 게시글을 작성한 사용자를 나타내는 다대일 관계입니다.
     * 
     * 특징:
     * - 필수 관계 (NOT NULL)
     * - FetchType.LAZY로 지연 로딩 (성능 최적화)
     * - 작성자 삭제 시 게시글도 함께 삭제 (CASCADE)
     * - @JoinColumn으로 외래키 컬럼명 명시
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /**
     * 게시글 생성 시간
     * 
     * 게시글이 처음 작성된 시간입니다.
     * 
     * 특징:
     * - @CreationTimestamp로 자동 설정
     * - 수정 불가능 (updatable = false)
     * - 게시글 정렬의 기준으로 사용
     * - 감사(Audit) 정보
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 게시글 최종 수정 시간
     * 
     * 게시글이 마지막으로 수정된 시간입니다.
     * 
     * 특징:
     * - @UpdateTimestamp로 자동 업데이트
     * - 게시글 수정 시마다 갱신
     * - 최신 순 정렬에 활용
     * - 감사(Audit) 정보
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 게시글 첨부 파일 목록
     * 
     * 이 게시글에 첨부된 파일들을 나타내는 일대다 관계입니다.
     * 
     * 특징:
     * - 한 게시글은 여러 파일을 가질 수 있음
     * - FetchType.LAZY로 지연 로딩 (필요할 때만 조회)
     * - CascadeType.ALL로 게시글 삭제 시 첨부파일도 함께 삭제
     * - orphanRemoval로 관계가 끊어진 파일 자동 삭제
     */
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, 
               cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<FileEntity> files = new HashSet<>();

    /**
     * 첨부 파일 추가 편의 메서드
     * 
     * 게시글에 새로운 첨부 파일을 추가합니다.
     * 양방향 관계를 정확히 설정하기 위해 파일의 게시글 참조도 설정합니다.
     * 
     * 사용 예시:
     * Post post = new Post();
     * FileEntity file = new FileEntity();
     * post.addFile(file);
     * 
     * @param file 추가할 첨부 파일
     */
    public void addFile(FileEntity file) {
        this.files.add(file);
        file.setPost(this);
    }

    /**
     * 첨부 파일 제거 편의 메서드
     * 
     * 게시글에서 특정 첨부 파일을 제거합니다.
     * 양방향 관계를 정확히 해제하기 위해 파일의 게시글 참조도 null로 설정합니다.
     * 
     * @param file 제거할 첨부 파일
     */
    public void removeFile(FileEntity file) {
        this.files.remove(file);
        file.setPost(null);
    }

    /**
     * 게시글 발행 처리
     * 
     * 임시 저장된 게시글을 발행 상태로 변경합니다.
     * 발행된 게시글은 모든 사용자에게 공개됩니다.
     */
    public void publish() {
        this.isPublished = true;
    }

    /**
     * 게시글 발행 취소 (임시 저장으로 변경)
     * 
     * 발행된 게시글을 임시 저장 상태로 변경합니다.
     * 임시 저장된 게시글은 작성자와 관리자만 조회할 수 있습니다.
     */
    public void unpublish() {
        this.isPublished = false;
    }

    /**
     * 발행된 게시글 여부 확인
     * 
     * 게시글이 발행 상태인지 확인합니다.
     * 
     * @return 발행된 게시글이면 true, 임시 저장이면 false
     */
    public boolean isPublished() {
        return this.isPublished != null && this.isPublished;
    }

    /**
     * 임시 저장된 게시글 여부 확인
     * 
     * 게시글이 임시 저장 상태인지 확인합니다.
     * 
     * @return 임시 저장된 게시글이면 true, 발행된 게시글이면 false
     */
    public boolean isDraft() {
        return !isPublished();
    }

    /**
     * 첨부 파일 존재 여부 확인
     * 
     * 게시글에 첨부된 파일이 있는지 확인합니다.
     * 
     * @return 첨부 파일이 있으면 true, 없으면 false
     */
    public boolean hasFiles() {
        return files != null && !files.isEmpty();
    }

    /**
     * 첨부 파일 개수 반환
     * 
     * 게시글에 첨부된 파일의 총 개수를 반환합니다.
     * 
     * @return 첨부 파일 개수
     */
    public int getFileCount() {
        return files != null ? files.size() : 0;
    }

    /**
     * 게시글 수정 가능 여부 확인
     * 
     * 특정 사용자가 이 게시글을 수정할 수 있는지 확인합니다.
     * 작성자 본인이거나 관리자 권한이 있어야 수정 가능합니다.
     * 
     * @param user 수정 권한을 확인할 사용자
     * @return 수정 가능하면 true, 불가능하면 false
     */
    public boolean canEdit(User user) {
        if (user == null) {
            return false;
        }
        return user.equals(this.author) || user.isAdmin();
    }

    /**
     * 작성자 확인
     * 
     * 특정 사용자가 이 게시글의 작성자인지 확인합니다.
     * 
     * @param user 확인할 사용자
     * @return 작성자이면 true, 아니면 false
     */
    public boolean isAuthor(User user) {
        return user != null && user.equals(this.author);
    }
}