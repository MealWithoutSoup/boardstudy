package com.blogapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 파일 엔티티
 * 
 * 이 클래스는 시스템에 업로드된 파일 정보를 나타내는 JPA 엔티티입니다.
 * 
 * 주요 기능:
 * - 업로드된 파일의 메타데이터 저장
 * - 원본 파일명과 서버 저장 파일명 구분 관리
 * - 업로드한 사용자(User)와 다대일 관계 매핑
 * - 첨부된 게시글(Post)과 다대일 관계 매핑 (선택적)
 * - 파일 보안을 위한 경로 및 크기 정보 관리
 * 
 * 설계 특징:
 * - 파일 시스템 보안을 위해 원본명과 저장명 분리
 * - MIME 타입으로 파일 형식 검증 지원
 * - 게시글 첨부는 선택적 (독립 파일 업로드도 지원)
 * - 업로드 시간 자동 추적
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-01-01
 */
@Entity
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"uploadedBy", "post"})
@EqualsAndHashCode(of = "id")
public class FileEntity {

    /**
     * 파일 고유 식별자
     * 
     * PostgreSQL의 BIGSERIAL 타입을 사용하여 자동 증가되는 기본키입니다.
     * GenerationType.IDENTITY를 사용하여 데이터베이스에서 자동 생성됩니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 원본 파일명
     * 
     * 사용자가 업로드한 파일의 원래 이름입니다.
     * 
     * 특징:
     * - 최대 255자까지 저장 가능
     * - 사용자에게 표시되는 파일명
     * - 다운로드 시 기본 파일명으로 사용
     * - 보안상 직접 서버 파일명으로 사용하지 않음
     * 
     * 예시: "내문서.pdf", "프로필사진.jpg"
     */
    @Column(name = "original_name", length = 255, nullable = false)
    private String originalName;

    /**
     * 서버 저장 파일명
     * 
     * 서버 파일 시스템에 실제로 저장되는 파일명입니다.
     * 
     * 특징:
     * - 최대 255자까지 저장 가능
     * - UUID 등을 활용한 유니크한 파일명
     * - 파일명 충돌 방지
     * - 보안상 추측하기 어려운 형태
     * 
     * 예시: "550e8400-e29b-41d4-a716-446655440000.pdf"
     */
    @Column(name = "stored_name", length = 255, nullable = false)
    private String storedName;

    /**
     * 파일 저장 경로
     * 
     * 서버에서 파일이 저장된 전체 경로입니다.
     * 
     * 특징:
     * - 최대 500자까지 저장 가능
     * - 절대 경로 또는 상대 경로
     * - 파일 시스템 접근을 위한 정보
     * - 백업 및 복구 시 활용
     * 
     * 예시: "/uploads/2024/01/550e8400-e29b-41d4-a716-446655440000.pdf"
     */
    @Column(name = "file_path", length = 500, nullable = false)
    private String filePath;

    /**
     * 파일 크기 (바이트)
     * 
     * 업로드된 파일의 크기를 바이트 단위로 저장합니다.
     * 
     * 특징:
     * - BIGINT 타입으로 대용량 파일 지원
     * - 업로드 제한 검증에 사용
     * - 스토리지 사용량 계산에 활용
     * - 다운로드 진행률 표시에 사용
     */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /**
     * 파일 MIME 타입
     * 
     * 파일의 형식을 나타내는 MIME 타입입니다.
     * 
     * 특징:
     * - 최대 100자까지 저장 가능
     * - 파일 형식 검증에 사용
     * - 다운로드 시 Content-Type 헤더로 활용
     * - 파일 형식별 처리 로직 분기에 사용
     * 
     * 예시: "image/jpeg", "application/pdf", "text/plain"
     */
    @Column(name = "content_type", length = 100, nullable = false)
    private String contentType;

    /**
     * 파일 업로드 사용자
     * 
     * 이 파일을 업로드한 사용자를 나타내는 다대일 관계입니다.
     * 
     * 특징:
     * - 필수 관계 (NOT NULL)
     * - FetchType.LAZY로 지연 로딩
     * - 파일 접근 권한 검증에 사용
     * - 사용자별 업로드 통계 산출에 활용
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    /**
     * 첨부된 게시글
     * 
     * 이 파일이 첨부된 게시글을 나타내는 다대일 관계입니다.
     * 
     * 특징:
     * - 선택적 관계 (NULL 허용)
     * - FetchType.LAZY로 지연 로딩
     * - 게시글과 독립적인 파일 업로드도 지원
     * - 게시글 삭제 시 첨부 파일은 NULL로 설정됨
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    /**
     * 파일 업로드 시간
     * 
     * 파일이 업로드된 시간입니다.
     * 
     * 특징:
     * - @CreationTimestamp로 자동 설정
     * - 수정 불가능 (updatable = false)
     * - 파일 관리 및 정리에 활용
     * - 감사(Audit) 정보
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 파일 확장자 추출
     * 
     * 원본 파일명에서 확장자를 추출합니다.
     * 
     * @return 파일 확장자 (점 포함, 예: ".pdf")
     *         확장자가 없으면 빈 문자열 반환
     */
    public String getFileExtension() {
        if (originalName == null) {
            return "";
        }
        int lastDotIndex = originalName.lastIndexOf('.');
        return lastDotIndex > 0 ? originalName.substring(lastDotIndex) : "";
    }

    /**
     * 파일 확장자 추출 (점 제외)
     * 
     * 원본 파일명에서 확장자를 추출합니다 (점 제외).
     * 
     * @return 파일 확장자 (점 제외, 예: "pdf")
     *         확장자가 없으면 빈 문자열 반환
     */
    public String getFileExtensionWithoutDot() {
        String extension = getFileExtension();
        return extension.startsWith(".") ? extension.substring(1) : extension;
    }

    /**
     * 사람이 읽기 쉬운 파일 크기 형식으로 변환
     * 
     * 바이트 단위의 파일 크기를 KB, MB, GB 단위로 변환합니다.
     * 
     * @return 형식화된 파일 크기 문자열 (예: "1.5 MB", "500 KB")
     */
    public String getFormattedFileSize() {
        if (fileSize == null) {
            return "0 B";
        }
        
        long bytes = fileSize;
        if (bytes < 1024) {
            return bytes + " B";
        }
        
        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return String.format("%.1f KB", kb);
        }
        
        double mb = kb / 1024.0;
        if (mb < 1024) {
            return String.format("%.1f MB", mb);
        }
        
        double gb = mb / 1024.0;
        return String.format("%.1f GB", gb);
    }

    /**
     * 이미지 파일 여부 확인
     * 
     * MIME 타입을 통해 이 파일이 이미지인지 확인합니다.
     * 
     * @return 이미지 파일이면 true, 아니면 false
     */
    public boolean isImage() {
        return contentType != null && contentType.startsWith("image/");
    }

    /**
     * 문서 파일 여부 확인
     * 
     * MIME 타입을 통해 이 파일이 문서인지 확인합니다.
     * 
     * @return 문서 파일이면 true, 아니면 false
     */
    public boolean isDocument() {
        if (contentType == null) {
            return false;
        }
        return contentType.equals("application/pdf") ||
               contentType.startsWith("application/vnd.openxmlformats-officedocument") ||
               contentType.startsWith("application/vnd.ms-") ||
               contentType.equals("text/plain") ||
               contentType.equals("application/rtf");
    }

    /**
     * 비디오 파일 여부 확인
     * 
     * MIME 타입을 통해 이 파일이 비디오인지 확인합니다.
     * 
     * @return 비디오 파일이면 true, 아니면 false
     */
    public boolean isVideo() {
        return contentType != null && contentType.startsWith("video/");
    }

    /**
     * 오디오 파일 여부 확인
     * 
     * MIME 타입을 통해 이 파일이 오디오인지 확인합니다.
     * 
     * @return 오디오 파일이면 true, 아니면 false
     */
    public boolean isAudio() {
        return contentType != null && contentType.startsWith("audio/");
    }

    /**
     * 파일 접근 권한 확인
     * 
     * 특정 사용자가 이 파일에 접근할 수 있는지 확인합니다.
     * 업로드한 사용자이거나 관리자 권한이 있어야 접근 가능합니다.
     * 
     * @param user 접근 권한을 확인할 사용자
     * @return 접근 가능하면 true, 불가능하면 false
     */
    public boolean canAccess(User user) {
        if (user == null) {
            return false;
        }
        return user.equals(this.uploadedBy) || user.isAdmin();
    }

    /**
     * 게시글 첨부 파일 여부 확인
     * 
     * 이 파일이 특정 게시글에 첨부된 파일인지 확인합니다.
     * 
     * @return 게시글에 첨부된 파일이면 true, 독립 파일이면 false
     */
    public boolean isAttachedToPost() {
        return post != null;
    }

    /**
     * 독립 파일 여부 확인
     * 
     * 이 파일이 게시글에 첨부되지 않은 독립 파일인지 확인합니다.
     * 
     * @return 독립 파일이면 true, 게시글 첨부 파일이면 false
     */
    public boolean isStandaloneFile() {
        return post == null;
    }
}