package com.minhyung.schedule.notification.domain;

import com.minhyung.schedule.common.CodeEnum;

public enum SendingStatus implements CodeEnum<Byte> {
    PENDING(0),         // 대기
    READY(1),           // 준비
    PROGRESSING(2),     // 진행
    SENT(3),            // 전송
    FAILED(4),          // 실패
    RETRY_PENDING(5);   // 재시도

    private final byte code;

    SendingStatus(int code) {
        this.code = (byte) code;
    }

    @Override
    public Byte getCode() {
        return code;
    }
}
