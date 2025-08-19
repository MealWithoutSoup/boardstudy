# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot blog application with JWT authentication and file upload capabilities. The project demonstrates a complete enterprise-level application structure with security, data persistence, file management, and comprehensive testing.

## 한글 개발 가이드 (Korean Development Guide)

### 프로젝트 소개
JWT 인증과 파일 업로드 기능을 가진 Spring Boot 기반의 블로그 애플리케이션입니다. 
신입 개발자도 이해할 수 있도록 모든 코드에 상세한 JavaDoc 주석이 포함되어 있습니다.

### 기술 스택
- **프레임워크**: Spring Boot 3.x
- **보안**: Spring Security + JWT
- **데이터베이스**: PostgreSQL (JPA + MyBatis 병행)
- **테스트**: JUnit 5 + TestContainers
- **로깅**: SLF4J + Logback
- **빌드도구**: Gradle

### 개발 순서 및 학습 경로

#### 1단계: 프로젝트 기반 설정
- **목적**: 프로젝트의 기본 구조와 설정을 이해
- **학습내용**: Gradle 설정, 패키지 구조, Git 관리
- **파일**: `build.gradle`, 기본 디렉토리 구조

#### 2단계: 의존성 설정 및 이해
- **목적**: Spring Boot 생태계와 각 라이브러리의 역할 이해
- **학습내용**: 29개 주요 의존성의 용도와 사용법
- **파일**: `build.gradle`의 dependencies 블록

#### 3단계: 데이터베이스 설계
- **목적**: JPA 엔티티 관계와 데이터베이스 스키마 설계
- **학습내용**: 엔티티 매핑, 관계 설정, 제약 조건
- **파일**: `User.java`, `Role.java`, `Post.java`, `FileEntity.java`

#### 4단계: 보안 계층 구현
- **목적**: JWT 기반 인증/인가 시스템 구축
- **학습내용**: Spring Security 설정, JWT 토큰 처리, 사용자 인증
- **파일**: 
  - `JwtTokenUtil.java` - JWT 토큰 생성/검증
  - `CustomUserDetails.java` - 사용자 인증 정보
  - `SecurityConfig.java` - 보안 설정
  - `JwtAuthenticationFilter.java` - JWT 필터

#### 5단계: Repository 계층 구현
- **목적**: 데이터 접근 계층과 쿼리 작성법 학습
- **학습내용**: JPA Repository, 커스텀 쿼리, MyBatis 매퍼
- **파일**: 
  - `UserRepository.java` - JPA 기반 사용자 데이터 접근
  - `UserMapper.java` - MyBatis 기반 쿼리 (학습용)

#### 6단계: Service 계층 구현
- **목적**: 비즈니스 로직과 트랜잭션 관리 학습
- **학습내용**: 서비스 패턴, 트랜잭션, 예외 처리
- **파일**: 
  - `UserService.java` - 사용자 관리 비즈니스 로직
  - `FileService.java` - 파일 처리 비즈니스 로직

#### 7단계: Controller 계층 구현
- **목적**: REST API 설계와 HTTP 통신 학습
- **학습내용**: RESTful API, HTTP 상태코드, API 문서화
- **파일**: 
  - `AuthController.java` - 인증 관련 API 엔드포인트

#### 8단계: 파일 업로드 시스템
- **목적**: 파일 처리와 보안 고려사항 학습
- **학습내용**: MultipartFile 처리, 파일 검증, 보안
- **파일**: `FileService.java`의 파일 업로드 메서드

#### 9단계: 로깅 및 모니터링
- **목적**: 애플리케이션 운영과 디버깅을 위한 로깅 시스템
- **학습내용**: 로그 레벨, 환경별 설정, 보안 로그
- **파일**: `logback-spring.xml`

#### 10단계: 테스트 구현
- **목적**: 테스트 주도 개발과 품질 보증
- **학습내용**: 단위 테스트, 통합 테스트, Mockito
- **파일**: `UserServiceTest.java`

### 커밋 히스토리를 통한 학습
각 단계별로 기능을 나누어 커밋했으므로, Git 히스토리를 따라가면서 
점진적으로 애플리케이션이 구축되는 과정을 학습할 수 있습니다.

### 주요 학습 포인트
1. **계층 구조**: Controller → Service → Repository → Entity
2. **의존성 주입**: Spring의 IoC 컨테이너 활용
3. **보안**: JWT 토큰 기반 stateless 인증
4. **데이터베이스**: JPA/Hibernate ORM 및 MyBatis
5. **테스트**: 단위/통합 테스트 작성법
6. **설정**: 환경별 설정 분리 및 관리

## Development Commands

**Build the project:**
```bash
./gradlew build
```

**Run the application:**
```bash
./gradlew bootRun
```

**Run tests:**
```bash
./gradlew test
```

**Run a single test class:**
```bash
./gradlew test --tests "UserServiceTest"
```

**Run integration tests:**
```bash
./gradlew integrationTest
```

**Clean build:**
```bash
./gradlew clean build
```

**Generate test report:**
```bash
./gradlew test jacocoTestReport
```

## Project Structure

- **Package structure:** `com.blogapp` (Spring Boot application package)
- **Build system:** Gradle with Spring Boot plugin
- **Testing framework:** JUnit 5 + TestContainers + Mockito
- **Java version:** 17+ (LTS)
- **Database:** PostgreSQL with JPA/Hibernate + MyBatis

### Directory Structure
```
src/
├── main/
│   ├── java/com/blogapp/
│   │   ├── entity/          # JPA entities
│   │   ├── repository/      # Data access layer
│   │   ├── service/         # Business logic
│   │   ├── controller/      # REST controllers
│   │   ├── security/        # Security configuration
│   │   ├── dto/            # Data transfer objects
│   │   └── exception/       # Custom exceptions
│   └── resources/
│       ├── application.yml  # Configuration
│       └── logback-spring.xml # Logging configuration
└── test/
    ├── java/com/blogapp/
    │   ├── unit/           # Unit tests
    │   └── integration/    # Integration tests
    └── resources/
        └── application-test.yml # Test configuration
```

## Architecture Notes

This is a comprehensive Spring Boot application demonstrating enterprise-level patterns:

### Technology Stack
- **Spring Boot 3.x** - Main framework
- **Spring Security** - Authentication/Authorization
- **Spring Data JPA** - Database access with Hibernate
- **PostgreSQL** - Primary database
- **JWT** - Stateless authentication
- **SLF4J + Logback** - Logging framework
- **JUnit 5** - Testing framework
- **TestContainers** - Integration testing with real database

### Architecture Patterns
- **3-Layer Architecture**: Controller → Service → Repository
- **DTO Pattern**: Separate data transfer objects from entities
- **Repository Pattern**: Abstract data access logic
- **Dependency Injection**: Spring IoC container
- **AOP**: Aspect-oriented programming for cross-cutting concerns

### Key Features
- JWT-based authentication
- Role-based access control (RBAC)
- File upload with security validation
- Comprehensive logging strategy
- Both JPA and MyBatis for learning purposes
- Unit and integration testing
- API documentation with Swagger

## 🚀 최근 완성된 작업 히스토리 (2024-08-19)

### 1. 프로젝트 GitHub 연동 및 초기 푸시
- **작업 내용**: 로컬 저장소를 https://github.com/MealWithoutSoup/boardstudy.git 에 연결
- **결과**: 11개 커밋 히스토리 전체 푸시 완료
- **의미**: 학습 과정의 단계별 커밋들이 GitHub에 기록되어 점진적 학습 가능

### 2. 테스트 시스템 완성 (핵심 작업)
- **문제 발견**: 초기 설계 대비 테스트 구현이 미완성 상태
- **사용자 피드백**: "테스트에서 너가 계획하고 설계한대로 완성되지 않았어 다시봐봐"
- **해결 과정**:
  
#### A. 누락된 컴포넌트 구현
- **DTO 클래스**: 5개 완성
  - `LoginRequest.java` - 로그인 요청, 보안 toString 구현
  - `UserRegistrationRequest.java` - 회원가입, Bean Validation 적용
  - `UserUpdateRequest.java` - 사용자 정보 수정
  - `JwtAuthenticationResponse.java` - JWT 응답, Builder 패턴
  - `UserResponse.java` - 사용자 정보 응답

- **예외 클래스**: 3개 완성
  - `DuplicateUsernameException.java` - 중복 사용자명 예외
  - `DuplicateEmailException.java` - 중복 이메일 예외
  - `UserNotFoundException.java` - 사용자 없음 예외

- **Repository 인터페이스**: 2개 추가
  - `RoleRepository.java` - 역할 관리
  - `FileRepository.java` - 파일 관리

#### B. 종합적인 테스트 시스템 구축
- **통합 테스트**: `UserServiceIntegrationTest.java` (12개 테스트 메서드)
  - 실제 Spring 컨텍스트 사용
  - @Transactional로 테스트 격리
  - 비즈니스 로직 end-to-end 검증

- **컨트롤러 테스트**: `AuthControllerTest.java` (8개 테스트 메서드)
  - @WebMvcTest로 웹 레이어만 테스트
  - MockMvc를 통한 HTTP 요청/응답 검증
  - JSON 직렬화/역직렬화 테스트

- **리포지터리 테스트**: `UserRepositoryTest.java` (10개 테스트 메서드)
  - @DataJpaTest로 JPA 레이어만 테스트
  - H2 인메모리 데이터베이스 사용
  - 커스텀 쿼리 메서드 검증

- **보안 테스트**: `SecurityConfigTest.java` (8개 테스트 메서드)
  - Spring Security 설정 검증
  - 인증/인가 시나리오 테스트
  - JWT 토큰 검증 테스트

- **파일 서비스 테스트**: `FileServiceTest.java` (16개 테스트 메서드)
  - 파일 업로드/다운로드 기능 테스트
  - 보안 검증 및 예외 처리 테스트
  - 임시 파일 시스템 활용

#### C. 테스트 설정 및 도구 완성
- **JaCoCo 테스트 커버리지**: 80% 전체, 85% 클래스별 목표
- **테스트 프로필**: `application-test.yml` H2 데이터베이스 설정
- **빌드 설정**: `build.gradle`에 테스트 태스크 및 검증 규칙 추가

### 3. ERD (Entity Relationship Diagram) 설계 문서화
- **작업 내용**: README.md에 포괄적인 데이터베이스 설계 문서 추가
- **포함 사항**:
  - Mermaid 형식의 시각적 ERD 다이어그램
  - 5개 핵심 엔티티 상세 설명 (User, Role, Post, FileEntity, UserRoles)
  - SQL DDL 문장 및 제약조건
  - 비즈니스 규칙 및 데이터 무결성 정책
  - 성능 최적화 인덱스 설계 (8개 인덱스)

### 4. Git 관리 및 협업 워크플로우
- **Git 충돌 해결**: rebase를 통한 원격 변경사항 통합
- **커밋 메시지 표준화**: 기능별 상세 커밋 메시지 작성
- **브랜치 관리**: main 브랜치 중심의 선형 히스토리 유지

## 📊 완성된 프로젝트 통계

### 코드 통계
- **엔티티**: 4개 (User, Role, Post, FileEntity) + JPA 관계 설정
- **DTO**: 5개 (요청/응답 객체, Bean Validation 적용)
- **예외 클래스**: 3개 (비즈니스 로직 예외 처리)
- **리포지터리**: 4개 (JPA + 커스텀 쿼리)
- **서비스**: 2개 (비즈니스 로직 계층)
- **컨트롤러**: 1개 (REST API 엔드포인트)

### 테스트 통계
- **테스트 클래스**: 5개
- **테스트 메서드**: 54개+ (단위 + 통합 + 기능 테스트)
- **테스트 커버리지**: 80% 전체 목표, 85% 클래스별 목표
- **테스트 타입**: Unit, Integration, Web, Repository, Security

### 설정 및 문서
- **설정 파일**: 7개 (application.yml, logback.xml, test 설정 등)
- **빌드 설정**: Gradle 기반, 29개 의존성 관리
- **문서화**: README.md (300+ 라인), CLAUDE.md (200+ 라인), ERD 문서

## 🎯 학습자를 위한 핵심 포인트

### 1. 점진적 개발 프로세스
- 각 커밋이 하나의 기능 단위를 완성
- 의존성 → 엔티티 → 보안 → 서비스 → 컨트롤러 → 테스트 순서
- 실제 기업 프로젝트의 개발 워크플로우 경험

### 2. 테스트 주도 개발 (TDD) 실습
- 완전한 테스트 커버리지 구현
- 다양한 테스트 전략 (Unit, Integration, Mock)
- 테스트를 통한 코드 품질 보장

### 3. 엔터프라이즈 아키텍처 패턴
- 3계층 아키텍처 (Controller-Service-Repository)
- DTO 패턴으로 계층간 데이터 전송
- 예외 처리 및 보안 고려사항

### 4. 실무 도구 및 기술 활용
- Spring Boot 3.x 최신 기술 스택
- JWT 기반 Stateless 인증
- JPA/Hibernate ORM 및 관계 매핑
- 파일 업로드 보안 처리
- 로깅 및 모니터링 전략

## 💡 다음 확장 가능한 기능들

### 백엔드 확장
- 게시글 CRUD API 구현
- 파일 다운로드 API
- 사용자 프로필 관리 API
- 검색 및 페이징 기능
- 캐시 시스템 (Redis)

### 프론트엔드 연동
- React/Vue.js 클라이언트 구현
- JWT 토큰 관리
- 파일 업로드 UI
- 반응형 웹 디자인

### DevOps 및 배포
- Docker 컨테이너화
- CI/CD 파이프라인
- 클라우드 배포 (AWS/Azure)
- 모니터링 대시보드

### 성능 최적화
- 데이터베이스 쿼리 최적화
- 캐싱 전략
- 로드 밸런싱
- CDN 활용

이 프로젝트는 Spring Boot 기반 엔터프라이즈 애플리케이션의 완전한 학습 템플릿으로 활용할 수 있습니다.