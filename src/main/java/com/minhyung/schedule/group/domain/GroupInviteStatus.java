package com.minhyung.schedule.group.domain;

import com.minhyung.schedule.common.CodeEnum;

public enum GroupInviteStatus implements CodeEnum<Byte> {
    PENDING(0),     // 대기
    ACCEPTED(1),    // 수락
    DECLINED(2);    // 거절

    private final byte code;

    GroupInviteStatus(int code) {
        this.code = (byte) code;
    }

    public Byte getCode() {
        return code;
    }
}
