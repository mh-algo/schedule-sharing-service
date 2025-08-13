package com.minhyung.schedule.common;

import java.util.regex.Pattern;

public final class PasswordRules {
    private PasswordRules() {}
    public static final String REGEX = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,20}";
    public static final Pattern PATTERN = Pattern.compile(REGEX);
}
