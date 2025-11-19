package com.minhyung.schedule.group.exception;

import com.minhyung.schedule.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GroupErrorCode implements ErrorCode {
    INVALID_ACCESS(HttpStatus.FORBIDDEN, "GROUP_001", "접근 권한이 없습니다."),
    USER_ALREADY_EXISTS_OR_INVITED(HttpStatus.CONFLICT, "GROUP_002", "이미 그룹에 속한 사용자이거나, 이미 그룹에 초대된 사용자입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
