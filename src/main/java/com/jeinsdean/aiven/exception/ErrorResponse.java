package com.jeinsdean.aiven.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API 에러 응답 표준 포맷
 * 모바일 앱에서 일관된 에러 처리를 위한 구조화된 응답
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final String code;
    private final String message;
    private final LocalDateTime timestamp;
    private final String path;
    private final List<FieldErrorDetail> errors;

    /**
     * 단일 에러 응답 생성
     */
    public static ErrorResponse of(ErrorCode errorCode, String path) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }

    /**
     * Validation 에러 응답 생성
     */
    public static ErrorResponse of(ErrorCode errorCode, String path, List<FieldError> fieldErrors) {
        List<FieldErrorDetail> errors = fieldErrors.stream()
                .map(FieldErrorDetail::of)
                .toList();

        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .timestamp(LocalDateTime.now())
                .path(path)
                .errors(errors)
                .build();
    }

    /**
     * 커스텀 메시지 에러 응답
     */
    public static ErrorResponse of(ErrorCode errorCode, String path, String customMessage) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(customMessage)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }

    /**
     * Validation 필드 에러 상세
     */
    @Getter
    @Builder
    public static class FieldErrorDetail {
        private final String field;
        private final String value;
        private final String reason;

        public static FieldErrorDetail of(FieldError fieldError) {
            return FieldErrorDetail.builder()
                    .field(fieldError.getField())
                    .value(fieldError.getRejectedValue() != null ?
                            fieldError.getRejectedValue().toString() : "")
                    .reason(fieldError.getDefaultMessage())
                    .build();
        }
    }
}