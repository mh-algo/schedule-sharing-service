package com.minhyung.schedule.auth.exception;

import com.minhyung.schedule.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SignupErrorCode implements ErrorCode {
    DUPLICATED_USERNAME(HttpStatus.CONFLICT, "SIGNUP_001", "이미 존재하는 아이디입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
