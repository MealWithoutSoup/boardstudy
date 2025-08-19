package com.blogapp.unit.service;

import com.blogapp.entity.FileEntity;
import com.blogapp.entity.User;
import com.blogapp.repository.FileRepository;
import com.blogapp.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * FileService 단위 테스트
 * 
 * 파일 업로드 서비스의 비즈니스 로직을 단위 테스트로 검증합니다.
 * 실제 파일 시스템 대신 임시 디렉토리를 사용하여 격리된 테스트를 수행합니다.
 * 
 * 테스트 범위:
 * - 파일 업로드 처리 로직
 * - 파일 검증 (크기, 타입, 이름)
 * - 보안 검사 및 제한사항
 * - 파일 메타데이터 관리
 * - 에러 처리 및 예외 상황
 * 
 * Mock 대상:
 * - FileRepository: 파일 메타데이터 저장
 * 
 * 실제 검증:
 * - 파일 시스템 작업 (임시 디렉토리 사용)
 * - 파일 검증 로직
 * - 보안 제약 사항
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FileService 단위 테스트")
class FileServiceTest {

    @Mock
    private FileRepository fileRepository;
    
    @InjectMocks
    private FileService fileService;
    
    @TempDir
    Path tempDir;
    
    private User testUser;
    private MultipartFile validImageFile;
    private MultipartFile validTextFile;
    private MultipartFile invalidTypeFile;
    private MultipartFile oversizedFile;
    
    @BeforeEach
    void setUp() {
        // 테스트 데이터 준비
        setupTestData();
        setupFileService();
    }
    
    /**
     * 테스트 데이터 초기화
     */
    private void setupTestData() {
        // 테스트 사용자
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .firstName("Test")
                .lastName("User")
                .isActive(true)
                .build();
        
        // 유효한 이미지 파일
        validImageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
        
        // 유효한 텍스트 파일
        validTextFile = new MockMultipartFile(
                "text",
                "test-document.txt",
                "text/plain",
                "test document content".getBytes()
        );
        
        // 허용되지 않는 파일 타입
        invalidTypeFile = new MockMultipartFile(
                "executable",
                "malicious.exe",
                "application/x-executable",
                "malicious content".getBytes()
        );
        
        // 크기 제한을 초과하는 파일 (11MB)
        byte[] largeContent = new byte[11 * 1024 * 1024];
        oversizedFile = new MockMultipartFile(
                "large",
                "large-file.jpg",
                "image/jpeg",
                largeContent
        );
    }
    
    /**
     * FileService 설정 초기화
     */
    private void setupFileService() {
        // 테스트용 설정값 주입
        ReflectionTestUtils.setField(fileService, "uploadDir", tempDir.toString());
        ReflectionTestUtils.setField(fileService, "maxFileSize", 10L * 1024 * 1024); // 10MB
        ReflectionTestUtils.setField(fileService, "allowedTypes", 
                List.of("image/jpeg", "image/png", "image/gif", "application/pdf", "text/plain"));
    }
    
    @Test
    @DisplayName("이미지 파일 업로드 성공")
    void uploadFile_ValidImage_Success() throws IOException {
        // Given
        FileEntity expectedFileEntity = FileEntity.builder()
                .id(1L)
                .originalName("test-image.jpg")
                .storedName("stored-name.jpg")
                .filePath(tempDir.resolve("stored-name.jpg").toString())
                .fileSize((long) validImageFile.getSize())
                .contentType("image/jpeg")
                .uploadedBy(testUser)
                .build();
        
        when(fileRepository.save(any(FileEntity.class))).thenReturn(expectedFileEntity);
        
        // When
        FileEntity result = fileService.uploadFile(validImageFile, testUser);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOriginalName()).isEqualTo("test-image.jpg");
        assertThat(result.getContentType()).isEqualTo("image/jpeg");
        assertThat(result.getFileSize()).isEqualTo(validImageFile.getSize());
        assertThat(result.getUploadedBy()).isEqualTo(testUser);
        
        // 파일이 실제로 저장되었는지 확인
        assertThat(Files.list(tempDir)).hasSize(1);
        
        verify(fileRepository).save(any(FileEntity.class));
    }
    
    @Test
    @DisplayName("텍스트 파일 업로드 성공")
    void uploadFile_ValidText_Success() throws IOException {
        // Given
        FileEntity expectedFileEntity = FileEntity.builder()
                .id(2L)
                .originalName("test-document.txt")
                .storedName("stored-doc.txt")
                .filePath(tempDir.resolve("stored-doc.txt").toString())
                .fileSize((long) validTextFile.getSize())
                .contentType("text/plain")
                .uploadedBy(testUser)
                .build();
        
        when(fileRepository.save(any(FileEntity.class))).thenReturn(expectedFileEntity);
        
        // When
        FileEntity result = fileService.uploadFile(validTextFile, testUser);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOriginalName()).isEqualTo("test-document.txt");
        assertThat(result.getContentType()).isEqualTo("text/plain");
        
        verify(fileRepository).save(any(FileEntity.class));
    }
    
    @Test
    @DisplayName("빈 파일 업로드 실패")
    void uploadFile_EmptyFile_ThrowsException() {
        // Given
        MultipartFile emptyFile = new MockMultipartFile(
                "empty",
                "empty.txt",
                "text/plain",
                new byte[0]
        );
        
        // When & Then
        assertThatThrownBy(() -> fileService.uploadFile(emptyFile, testUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("빈 파일은 업로드할 수 없습니다");
        
        verify(fileRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("크기 제한 초과 파일 업로드 실패")
    void uploadFile_OversizedFile_ThrowsException() {
        // When & Then
        assertThatThrownBy(() -> fileService.uploadFile(oversizedFile, testUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("파일 크기가 제한을 초과했습니다");
        
        verify(fileRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("허용되지 않는 파일 타입 업로드 실패")
    void uploadFile_InvalidFileType_ThrowsException() {
        // When & Then
        assertThatThrownBy(() -> fileService.uploadFile(invalidTypeFile, testUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("허용되지 않는 파일 형식입니다");
        
        verify(fileRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("고유한 파일명 생성 확인")
    void uploadFile_GeneratesUniqueFilename() throws IOException {
        // Given
        MultipartFile file1 = new MockMultipartFile(
                "file1", "same-name.jpg", "image/jpeg", "content1".getBytes());
        MultipartFile file2 = new MockMultipartFile(
                "file2", "same-name.jpg", "image/jpeg", "content2".getBytes());
        
        FileEntity fileEntity1 = FileEntity.builder()
                .id(1L).originalName("same-name.jpg").storedName("uuid1.jpg")
                .filePath("path1").fileSize(8L).contentType("image/jpeg")
                .uploadedBy(testUser).build();
        
        FileEntity fileEntity2 = FileEntity.builder()
                .id(2L).originalName("same-name.jpg").storedName("uuid2.jpg")
                .filePath("path2").fileSize(8L).contentType("image/jpeg")
                .uploadedBy(testUser).build();
        
        when(fileRepository.save(any(FileEntity.class)))
                .thenReturn(fileEntity1)
                .thenReturn(fileEntity2);
        
        // When
        FileEntity result1 = fileService.uploadFile(file1, testUser);
        FileEntity result2 = fileService.uploadFile(file2, testUser);
        
        // Then
        assertThat(result1.getStoredName()).isNotEqualTo(result2.getStoredName());
        assertThat(Files.list(tempDir)).hasSize(2);
        
        verify(fileRepository, times(2)).save(any(FileEntity.class));
    }
    
    @Test
    @DisplayName("업로드 디렉토리 자동 생성")
    void uploadFile_CreatesUploadDirectory() throws IOException {
        // Given - 존재하지 않는 하위 디렉토리 설정
        Path subDir = tempDir.resolve("uploads");
        ReflectionTestUtils.setField(fileService, "uploadDir", subDir.toString());
        
        FileEntity expectedFileEntity = FileEntity.builder()
                .id(1L)
                .originalName("test-image.jpg")
                .storedName("stored-name.jpg")
                .filePath(subDir.resolve("stored-name.jpg").toString())
                .fileSize((long) validImageFile.getSize())
                .contentType("image/jpeg")
                .uploadedBy(testUser)
                .build();
        
        when(fileRepository.save(any(FileEntity.class))).thenReturn(expectedFileEntity);
        
        // When
        fileService.uploadFile(validImageFile, testUser);
        
        // Then
        assertThat(Files.exists(subDir)).isTrue();
        assertThat(Files.isDirectory(subDir)).isTrue();
        assertThat(Files.list(subDir)).hasSize(1);
    }
    
    @Test
    @DisplayName("파일 확장자 보존 확인")
    void uploadFile_PreservesFileExtension() throws IOException {
        // Given
        MultipartFile pdfFile = new MockMultipartFile(
                "document", "test.pdf", "application/pdf", "pdf content".getBytes());
        
        FileEntity expectedFileEntity = FileEntity.builder()
                .id(1L)
                .originalName("test.pdf")
                .storedName("uuid-name.pdf")
                .filePath(tempDir.resolve("uuid-name.pdf").toString())
                .fileSize((long) pdfFile.getSize())
                .contentType("application/pdf")
                .uploadedBy(testUser)
                .build();
        
        when(fileRepository.save(any(FileEntity.class))).thenReturn(expectedFileEntity);
        
        // When
        FileEntity result = fileService.uploadFile(pdfFile, testUser);
        
        // Then
        assertThat(result.getStoredName()).endsWith(".pdf");
        
        // 실제 저장된 파일 확인
        Path[] savedFiles = Files.list(tempDir).toArray(Path[]::new);
        assertThat(savedFiles).hasSize(1);
        assertThat(savedFiles[0].getFileName().toString()).endsWith(".pdf");
    }
}