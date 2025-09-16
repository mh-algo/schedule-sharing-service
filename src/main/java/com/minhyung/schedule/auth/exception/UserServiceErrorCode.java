package com.minhyung.schedule.auth.exception;

import com.minhyung.schedule.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserServiceErrorCode implements ErrorCode {
    DUPLICATED_USERNAME(HttpStatus.CONFLICT, "USER_001", "이미 존재하는 아이디입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_002", "%s: 해당 사용자가 존재하지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
