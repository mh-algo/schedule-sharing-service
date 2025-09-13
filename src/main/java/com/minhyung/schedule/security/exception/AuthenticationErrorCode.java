package com.minhyung.schedule.security.exception;

import com.minhyung.schedule.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthenticationErrorCode implements ErrorCode {
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "AUTH_001", "허용되지 않는 메서드 요청입니다."),
    INVALID_JSON_PROPERTY(HttpStatus.BAD_REQUEST, "AUTH_002", "%s: 유효하지 않은 json property 입니다."),
    INVALID_JSON_FORMAT(HttpStatus.BAD_REQUEST, "AUTH_003", "요청한 형식이 올바르지 않습니다."),
    INVALID_AUTHENTICATION(HttpStatus.UNAUTHORIZED, "AUTH_004", "인증 정보가 유효하지 않습니다."),
    AUTHENTICATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_005", "사용자 인증에 실패했습니다."),
    NOT_AUTHENTICATED(HttpStatus.UNAUTHORIZED, "AUTH_006", "로그인이 필요합니다."),
    INVALID_ACCESS(HttpStatus.FORBIDDEN, "AUTH_007", "접근 권한이 없습니다."),
    AUTHORIZATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_008", "접근 권한 확인에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
