package com.minhyung.schedule.auth.domain;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum UserStatus {
    SUSPENDED(0),   // 정지
    ACTIVE(1),      // 활성
    UNVERIFIED(2);  // 미인증

    private final byte code;

    private static final Map<Byte, UserStatus> CODE_MAP =
            Arrays.stream(UserStatus.values())
                    .collect(Collectors.toUnmodifiableMap(v -> v.getCode(), Function.identity()));

    UserStatus(int code) {
        this.code = (byte) code;
    }

    public Byte getCode() {
        return code;
    }

    public static Byte getCode(UserStatus type) {
        return Objects.requireNonNull(type, "UserStatus is null").getCode();
    }

    public static UserStatus getType(Byte code) {
        Objects.requireNonNull(code, "code is null");
        return Objects.requireNonNull(CODE_MAP.get(code), "Invalid code! Not Found UserStatus");
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
