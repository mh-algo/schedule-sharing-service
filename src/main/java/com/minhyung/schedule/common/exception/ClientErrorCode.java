package com.minhyung.schedule.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ClientErrorCode implements ErrorCode {
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_001", "유효하지 않은 요청 경로입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_002", "유효하지 않은 메서드 요청입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
