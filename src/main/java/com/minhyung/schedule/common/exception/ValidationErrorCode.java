package com.minhyung.schedule.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ValidationErrorCode implements ErrorCode {
    VALIDATION_EXCEPTION(HttpStatus.BAD_REQUEST, "VALIDATION_001", "입력값이 유효하지 않습니다."),
    MISSING_REQUIRED_PARAM(HttpStatus.BAD_REQUEST, "VALIDATION_002", "%s: 필수 파라미터가 누락되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
