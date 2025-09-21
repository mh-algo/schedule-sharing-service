package com.minhyung.schedule.notification.domain;

import com.minhyung.schedule.common.CodeEnum;

public enum MessageType implements CodeEnum<Byte> {
    GROUP_INVITE(1);        // 그룹 초대

    private final byte code;

    MessageType(int code) {
        this.code = (byte) code;
    }

    @Override
    public Byte getCode() {
        return code;
    }
}
