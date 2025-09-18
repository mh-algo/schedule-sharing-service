package com.minhyung.schedule.group.domain;

import com.minhyung.schedule.common.CodeEnum;

public enum GroupMemberRoleType implements CodeEnum<Byte> {
    BLACK_LIST(0),      // 블랙리스트
    OWNER(1),           // 그룹장
    MANAGER(2),         // 관리자
    USER(3);            // 사용자

    private final byte code;

    GroupMemberRoleType(int code) {
        this.code = (byte) code;
    }

    @Override
    public Byte getCode() {
        return code;
    }
}
