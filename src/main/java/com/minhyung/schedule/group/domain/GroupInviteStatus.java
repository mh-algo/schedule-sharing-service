package com.minhyung.schedule.group.domain;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum GroupInviteStatus {
    PENDING(0),     // 대기
    ACCEPTED(1),    // 수락
    DECLINED(2);    // 거절

    private final byte code;

    private static final Map<Byte, GroupInviteStatus> CODE_MAP =
            Arrays.stream(GroupInviteStatus.values())
                    .collect(Collectors.toUnmodifiableMap(v -> v.getCode(), Function.identity()));

    GroupInviteStatus(int code) {
        this.code = (byte) code;
    }

    public Byte getCode() {
        return code;
    }

    public static Byte getCode(GroupInviteStatus type) {
        return Objects.requireNonNull(type, "UserInviteStatus is null").getCode();
    }

    public static GroupInviteStatus getType(Byte code) {
        Objects.requireNonNull(code, "code is null");
        return Objects.requireNonNull(CODE_MAP.get(code), "Invalid code! Not Found UserInviteStatus");
    }
}
