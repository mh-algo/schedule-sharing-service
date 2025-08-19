package com.minhyung.schedule.security.login.exception;

import com.minhyung.schedule.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum LoginErrorCode implements ErrorCode {
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "LOGIN_001", "아이디 또는 비밀번호가 잘못되었습니다."),
    ACCOUNT_SUSPENDED(HttpStatus.FORBIDDEN, "LOGIN_002", "계정이 정지된 상태입니다."),
    LOGIN_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "LOGIN_003", "로그인에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
