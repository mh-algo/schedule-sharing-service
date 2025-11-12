package com.minhyung.schedule.auth.domain;

import com.minhyung.schedule.common.CodeEnum;

public enum UserStatus implements CodeEnum<Byte> {
    SUSPENDED(0),   // 정지
    ACTIVE(1),      // 활성
    UNVERIFIED(2);  // 미인증

    private final byte code;

    UserStatus(int code) {
        this.code = (byte) code;
    }

    public Byte getCode() {
        return code;
    }

    public boolean isSuspended() {
        return this == SUSPENDED;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isUnverified() {
        return this == UNVERIFIED;
    }
}
