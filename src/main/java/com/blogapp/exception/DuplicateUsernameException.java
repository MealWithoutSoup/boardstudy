package com.blogapp.exception;

/**
 * 중복 사용자명 예외
 * 
 * 회원가입 시 이미 존재하는 사용자명으로 등록을 시도할 때 발생하는 예외입니다.
 * 사용자명은 시스템에서 고유해야 하므로 중복을 허용하지 않습니다.
 * 
 * 발생 시나리오:
 * - 회원가입 시 기존 사용자명과 동일한 이름 사용
 * - 사용자명 변경 시 다른 사용자의 사용자명과 충돌
 * 
 * 처리 방안:
 * - HTTP 409 Conflict 상태 코드 반환
 * - 클라이언트에게 사용자명 변경 요청
 * - 대안 사용자명 제안 (선택사항)
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-01-01
 */
public class DuplicateUsernameException extends RuntimeException {
    
    /**
     * 기본 생성자
     * 
     * 기본 메시지와 함께 예외를 생성합니다.
     */
    public DuplicateUsernameException() {
        super("이미 사용 중인 사용자명입니다.");
    }
    
    /**
     * 메시지를 포함한 생성자
     * 
     * 커스텀 메시지와 함께 예외를 생성합니다.
     * 
     * @param message 예외 메시지
     */
    public DuplicateUsernameException(String message) {
        super(message);
    }
    
    /**
     * 메시지와 원인을 포함한 생성자
     * 
     * 커스텀 메시지와 원인 예외를 함께 포함하여 예외를 생성합니다.
     * 
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    public DuplicateUsernameException(String message, Throwable cause) {
        super(message, cause);
    }
}