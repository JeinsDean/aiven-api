package com.jeinsdean.aiven.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 비즈니스 예외 코드 정의
 * 대규모 서비스에서는 에러 코드 체계화가 필수
 * 클라이언트(모바일)에서 에러 핸들링 및 다국어 처리 용이
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common (1000번대)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 에러가 발생했습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C004", "잘못된 타입입니다."),
    HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "C005", "접근 권한이 없습니다."),

    // Authentication (2000번대)
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "만료된 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A004", "유효하지 않은 리프레시 토큰입니다."),

    // User (3000번대)
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U002", "이미 사용중인 이메일입니다."),
    USER_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "U003", "이미 탈퇴한 사용자입니다."),

    // Suggestion (4000번대)
    SUGGESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "제안을 찾을 수 없습니다."),
    SUGGESTION_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S002", "제안 생성에 실패했습니다."),
    INVALID_SUGGESTION_CONTEXT(HttpStatus.BAD_REQUEST, "S003", "유효하지 않은 제안 컨텍스트입니다."),

    // AI (5000번대)
    AI_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AI001", "AI API 호출 중 오류가 발생했습니다."),
    AI_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "AI002", "AI 응답 시간이 초과되었습니다."),
    AI_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "AI003", "AI API 호출 한도를 초과했습니다."),
    ALL_AI_PROVIDERS_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "AI004", "모든 AI 서비스가 응답하지 않습니다."),

    // Location (6000번대)
    LOCATION_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "L001", "위치 서비스 오류가 발생했습니다."),
    INVALID_COORDINATES(HttpStatus.BAD_REQUEST, "L002", "유효하지 않은 좌표입니다."),

    // Feedback (7000번대)
    FEEDBACK_NOT_FOUND(HttpStatus.NOT_FOUND, "F001", "피드백을 찾을 수 없습니다."),
    DUPLICATE_FEEDBACK(HttpStatus.CONFLICT, "F002", "이미 피드백을 제출했습니다."),

    // Rate Limit (8000번대)
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "R001", "요청 한도를 초과했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}