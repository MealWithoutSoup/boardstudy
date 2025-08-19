package com.blogapp.repository;

import com.blogapp.entity.FileEntity;
import com.blogapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 파일 엔티티 Repository 인터페이스
 * 
 * 업로드된 파일의 메타데이터에 대한 데이터 액세스를 담당하는 Repository입니다.
 * 파일 관리, 검색, 통계 등의 기능을 제공합니다.
 * 
 * 주요 기능:
 * - 파일 메타데이터 저장 및 조회
 * - 사용자별 파일 목록 관리
 * - 파일 타입별 필터링
 * - 파일 사용량 통계
 * - 파일 보안 및 접근 제어
 * 
 * 보안 고려사항:
 * - 파일 접근 권한 검증
 * - 사용자별 파일 격리
 * - 파일 크기 및 타입 제한
 * - 악성 파일 검사 지원
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-01-01
 */
@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {
    
    /**
     * 사용자별 파일 목록 조회
     * 
     * 특정 사용자가 업로드한 모든 파일을 페이징하여 조회합니다.
     * 최신 업로드 순으로 정렬됩니다.
     * 
     * @param uploadedBy 파일을 업로드한 사용자
     * @param pageable 페이징 정보
     * @return 사용자의 파일 목록 (페이징)
     */
    Page<FileEntity> findByUploadedByOrderByUploadedAtDesc(User uploadedBy, Pageable pageable);
    
    /**
     * 저장된 파일명으로 조회
     * 
     * 서버에 저장된 고유한 파일명으로 파일 정보를 조회합니다.
     * 파일 다운로드나 서비스 시 사용됩니다.
     * 
     * @param storedName 서버에 저장된 파일명
     * @return 파일 정보 (Optional)
     */
    Optional<FileEntity> findByStoredName(String storedName);
    
    /**
     * 컨텐츠 타입별 파일 조회
     * 
     * 특정 MIME 타입의 파일들을 조회합니다.
     * 예: 이미지 파일만 조회, 문서 파일만 조회 등
     * 
     * @param contentType MIME 타입 (예: "image/jpeg", "application/pdf")
     * @param pageable 페이징 정보
     * @return 해당 타입의 파일 목록
     */
    Page<FileEntity> findByContentType(String contentType, Pageable pageable);
    
    /**
     * 파일 크기 범위로 조회
     * 
     * 지정된 크기 범위 내의 파일들을 조회합니다.
     * 저장 공간 관리나 대용량 파일 관리에 활용됩니다.
     * 
     * @param minSize 최소 파일 크기 (바이트)
     * @param maxSize 최대 파일 크기 (바이트)
     * @param pageable 페이징 정보
     * @return 크기 범위 내 파일 목록
     */
    Page<FileEntity> findByFileSizeBetween(Long minSize, Long maxSize, Pageable pageable);
    
    /**
     * 업로드 날짜 범위로 조회
     * 
     * 특정 기간 동안 업로드된 파일들을 조회합니다.
     * 백업이나 정리 작업 시 유용합니다.
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param pageable 페이징 정보
     * @return 해당 기간의 파일 목록
     */
    Page<FileEntity> findByUploadedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * 사용자별 총 파일 크기 조회
     * 
     * 특정 사용자가 업로드한 모든 파일의 총 크기를 계산합니다.
     * 저장 공간 할당량 관리에 사용됩니다.
     * 
     * @param uploadedBy 사용자
     * @return 총 파일 크기 (바이트)
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileEntity f WHERE f.uploadedBy = :user")
    Long getTotalFileSizeByUser(@Param("user") User uploadedBy);
    
    /**
     * 사용자별 파일 개수 조회
     * 
     * 특정 사용자가 업로드한 파일의 총 개수를 조회합니다.
     * 
     * @param uploadedBy 사용자
     * @return 파일 개수
     */
    Long countByUploadedBy(User uploadedBy);
    
    /**
     * 컨텐츠 타입별 통계 조회
     * 
     * 각 파일 타입별로 파일 개수와 총 크기를 조회합니다.
     * 관리자 대시보드에서 통계 정보로 활용됩니다.
     * 
     * @return 타입별 통계 (Object 배열: [타입, 개수, 총크기])
     */
    @Query("SELECT f.contentType, COUNT(f), SUM(f.fileSize) FROM FileEntity f " +
           "GROUP BY f.contentType " +
           "ORDER BY COUNT(f) DESC")
    List<Object[]> getFileStatisticsByContentType();
    
    /**
     * 고아 파일 조회
     * 
     * 게시글이나 다른 엔티티와 연결되지 않은 파일들을 조회합니다.
     * 정기적인 정리 작업에서 사용됩니다.
     * 
     * @param cutoffDate 기준 날짜 (이전에 업로드된 파일 대상)
     * @return 고아 파일 목록
     */
    @Query("SELECT f FROM FileEntity f WHERE f.post IS NULL AND f.uploadedAt < :cutoffDate")
    List<FileEntity> findOrphanedFiles(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * 이미지 파일 조회
     * 
     * 이미지 타입의 파일들만 조회합니다.
     * 갤러리나 이미지 관리 기능에서 사용됩니다.
     * 
     * @param pageable 페이징 정보
     * @return 이미지 파일 목록
     */
    @Query("SELECT f FROM FileEntity f WHERE f.contentType LIKE 'image/%' ORDER BY f.uploadedAt DESC")
    Page<FileEntity> findImageFiles(Pageable pageable);
}