package com.blogapp.exception;

/**
 * 중복 이메일 예외
 * 
 * 회원가입이나 프로필 수정 시 이미 존재하는 이메일 주소를 사용하려 할 때 발생하는 예외입니다.
 * 이메일 주소는 시스템에서 고유해야 하므로 중복을 허용하지 않습니다.
 * 
 * 발생 시나리오:
 * - 회원가입 시 기존 사용자의 이메일과 동일한 주소 사용
 * - 프로필 수정 시 다른 사용자의 이메일 주소로 변경 시도
 * - 이메일 인증 과정에서 중복 확인
 * 
 * 처리 방안:
 * - HTTP 409 Conflict 상태 코드 반환
 * - 클라이언트에게 다른 이메일 주소 사용 요청
 * - 기존 계정 찾기 링크 제공 (선택사항)
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-01-01
 */
public class DuplicateEmailException extends RuntimeException {
    
    /**
     * 기본 생성자
     * 
     * 기본 메시지와 함께 예외를 생성합니다.
     */
    public DuplicateEmailException() {
        super("이미 사용 중인 이메일 주소입니다.");
    }
    
    /**
     * 메시지를 포함한 생성자
     * 
     * 커스텀 메시지와 함께 예외를 생성합니다.
     * 
     * @param message 예외 메시지
     */
    public DuplicateEmailException(String message) {
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
    public DuplicateEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}