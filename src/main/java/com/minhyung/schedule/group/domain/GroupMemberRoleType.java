package com.minhyung.schedule.group.domain;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum GroupMemberRoleType {
    BLACK_LIST(0),      // 블랙리스트
    OWNER(1),           // 그룹장
    MANAGER(2),         // 관리자
    USER(3);            // 사용자

    private final byte code;

    private static final Map<Byte, GroupMemberRoleType> CODE_MAP =
            Arrays.stream(GroupMemberRoleType.values())
                    .collect(Collectors.toUnmodifiableMap(v -> v.getCode(), Function.identity()));

    GroupMemberRoleType(int code) {
        this.code = (byte) code;
    }

    public Byte getCode() {
        return code;
    }

    public static Byte getCode(GroupMemberRoleType type) {
        return Objects.requireNonNull(type, "MemberRoleType is null").getCode();
    }

    public static GroupMemberRoleType getType(Byte code) {
        Objects.requireNonNull(code, "code is null");
        return Objects.requireNonNull(CODE_MAP.get(code), "Invalid code! Not Found MemberRoleType");
    }
}
