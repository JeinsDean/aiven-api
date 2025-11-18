package com.jeinsdean.aiven.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 전역 예외 처리 핸들러
 * 모든 컨트롤러에서 발생하는 예외를 일관된 형식으로 처리
 *
 * 대규모 서비스에서는 예외 로깅과 모니터링이 핵심
 * - 비즈니스 예외: INFO 레벨 (정상 흐름)
 * - 시스템 예외: ERROR 레벨 (장애 알림 필요)
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 예외 처리
     * 예상 가능한 예외로 클라이언트에게 명확한 에러 메시지 전달
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException e,
            HttpServletRequest request) {

        ErrorCode errorCode = e.getErrorCode();
        log.info("Business exception occurred: code={}, message={}, path={}",
                errorCode.getCode(), e.getMessage(), request.getRequestURI());

        ErrorResponse response = ErrorResponse.of(errorCode, request.getRequestURI());
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    /**
     * Validation 예외 처리 (@Valid, @Validated)
     * 필드별 에러 상세 정보 제공
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e,
            HttpServletRequest request) {

        log.info("Validation failed: path={}, errors={}",
                request.getRequestURI(), e.getBindingResult().getFieldErrors().size());

        ErrorResponse response = ErrorResponse.of(
                ErrorCode.INVALID_INPUT_VALUE,
                request.getRequestURI(),
                e.getBindingResult().getFieldErrors()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * BindException 처리 (폼 바인딩 에러)
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(
            BindException e,
            HttpServletRequest request) {

        log.info("Bind exception occurred: path={}", request.getRequestURI());

        ErrorResponse response = ErrorResponse.of(
                ErrorCode.INVALID_INPUT_VALUE,
                request.getRequestURI(),
                e.getBindingResult().getFieldErrors()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 타입 불일치 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e,
            HttpServletRequest request) {

        log.info("Type mismatch: parameter={}, path={}", e.getName(), request.getRequestURI());

        ErrorResponse response = ErrorResponse.of(
                ErrorCode.INVALID_TYPE_VALUE,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 지원하지 않는 HTTP 메서드 예외 처리
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e,
            HttpServletRequest request) {

        log.info("Method not allowed: method={}, path={}", e.getMethod(), request.getRequestURI());

        ErrorResponse response = ErrorResponse.of(
                ErrorCode.METHOD_NOT_ALLOWED,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    /**
     * 접근 권한 예외 처리
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException e,
            HttpServletRequest request) {

        log.warn("Access denied: path={}, message={}", request.getRequestURI(), e.getMessage());

        ErrorResponse response = ErrorResponse.of(
                ErrorCode.HANDLE_ACCESS_DENIED,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * 처리되지 않은 모든 예외
     * 운영 환경에서는 상세 스택 트레이스를 노출하지 않음
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception e,
            HttpServletRequest request) {

        log.error("Unexpected exception occurred: path={}", request.getRequestURI(), e);

        ErrorResponse response = ErrorResponse.of(
                ErrorCode.INTERNAL_SERVER_ERROR,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}