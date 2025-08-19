package com.blogapp.exception;

/**
 * 사용자 조회 실패 예외
 * 
 * 요청된 사용자를 데이터베이스에서 찾을 수 없을 때 발생하는 예외입니다.
 * 사용자 ID, 사용자명, 이메일 등으로 조회 시 해당 사용자가 존재하지 않는 경우 발생합니다.
 * 
 * 발생 시나리오:
 * - 존재하지 않는 사용자 ID로 조회 시도
 * - 삭제되거나 비활성화된 사용자 계정 접근
 * - 잘못된 사용자명이나 이메일로 검색
 * - JWT 토큰의 사용자 정보가 유효하지 않은 경우
 * 
 * 처리 방안:
 * - HTTP 404 Not Found 상태 코드 반환
 * - 보안상 구체적인 정보는 노출하지 않음
 * - 로그인 재시도 또는 계정 복구 안내
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-01-01
 */
public class UserNotFoundException extends RuntimeException {
    
    /**
     * 기본 생성자
     * 
     * 기본 메시지와 함께 예외를 생성합니다.
     */
    public UserNotFoundException() {
        super("사용자를 찾을 수 없습니다.");
    }
    
    /**
     * 메시지를 포함한 생성자
     * 
     * 커스텀 메시지와 함께 예외를 생성합니다.
     * 
     * @param message 예외 메시지
     */
    public UserNotFoundException(String message) {
        super(message);
    }
    
    /**
     * 메시지와 원인을 포함한 생성자
     * 
     * 커스텀 메시지와 원인 예외를 함께 포함하여 예외 생성합니다.
     * 
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}