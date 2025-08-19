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