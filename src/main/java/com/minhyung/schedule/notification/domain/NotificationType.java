package com.minhyung.schedule.notification.domain;

import com.minhyung.schedule.common.CodeEnum;

public enum NotificationType implements CodeEnum<Byte> {
    INVITE_CREATED(0);      // 초대 생성

    private final byte code;

    NotificationType(int code) {
        this.code = (byte) code;
    }

    @Override
    public Byte getCode() {
        return code;
    }
}
