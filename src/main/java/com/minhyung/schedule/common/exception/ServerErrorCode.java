package com.minhyung.schedule.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ServerErrorCode implements ErrorCode {
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_001", "서버 내부 오류가 발생했습니다."),
    DB_CONSTRAINT_VIOLATION(HttpStatus.CONFLICT, "SERVER_002", "무결성 제약 조건이 위반된 요청입니다."),
    DB_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_003", "데이터베이스 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
