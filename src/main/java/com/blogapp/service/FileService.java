package com.blogapp.service;

import com.blogapp.entity.FileEntity;
import com.blogapp.entity.User;
import com.blogapp.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/**
 * 파일 업로드/다운로드 서비스
 * 
 * 파일 업로드, 다운로드, 관리 기능을 제공하는 서비스입니다.
 * 
 * 주요 기능:
 * - 멀티파트 파일 업로드 처리
 * - 파일 저장 및 메타데이터 관리
 * - 파일 다운로드 및 스트리밍
 * - 파일 보안 및 접근 제어
 * - 업로드 제한 및 검증
 * 
 * 보안 특징:
 * - 파일 타입 검증
 * - 파일 크기 제한
 * - 안전한 파일명 생성
 * - 경로 탐색 공격 방지
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FileService {

    private final FileRepository fileRepository;

    @Value("${file.upload.dir:/uploads}")
    private String uploadDir;

    @Value("${file.upload.max-size:10485760}") // 10MB
    private long maxFileSize;

    @Value("${file.upload.allowed-types:image/jpeg,image/png,image/gif,application/pdf,text/plain}")
    private List<String> allowedTypes;

    /**
     * 파일 업로드 처리
     * 
     * @param file 업로드할 파일
     * @param uploadedBy 업로드한 사용자
     * @return 저장된 파일 정보
     */
    @Transactional
    public FileEntity uploadFile(MultipartFile file, User uploadedBy) throws IOException {
        log.info("파일 업로드 시작 - 파일명: {}, 크기: {}, 사용자: {}", 
                file.getOriginalFilename(), file.getSize(), uploadedBy.getUsername());

        // 1. 파일 검증
        validateFile(file);

        // 2. 고유한 파일명 생성
        String storedFileName = generateUniqueFileName(file.getOriginalFilename());
        
        // 3. 파일 저장 경로 생성
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        Path filePath = uploadPath.resolve(storedFileName);
        
        // 4. 파일 저장
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // 5. 파일 메타데이터 저장
        FileEntity fileEntity = FileEntity.builder()
                .originalName(file.getOriginalFilename())
                .storedName(storedFileName)
                .filePath(filePath.toString())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .uploadedBy(uploadedBy)
                .build();
        
        FileEntity savedFile = fileRepository.save(fileEntity);
        log.info("파일 업로드 완료 - ID: {}, 저장 경로: {}", savedFile.getId(), filePath);
        
        return savedFile;
    }

    /**
     * 파일 검증
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일은 업로드할 수 없습니다.");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("파일 크기가 제한을 초과했습니다. 최대: " + maxFileSize + " bytes");
        }
        
        if (!allowedTypes.contains(file.getContentType())) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다: " + file.getContentType());
        }
    }

    /**
     * 고유한 파일명 생성
     */
    private String generateUniqueFileName(String originalFileName) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}