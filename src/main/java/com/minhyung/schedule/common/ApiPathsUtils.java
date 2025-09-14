package com.minhyung.schedule.common;

public final class ApiPathsUtils {
    public static String auth(Object... segments) {
        return join(ApiPaths.AUTH, segments);
    }

    public static String group(Object... segments) {
        return join(ApiPaths.GROUP, segments);
    }

    public static String join(String base, Object... segments) {
        if (base == null || base.isBlank()) {
            throw new IllegalArgumentException("base must not be blank");
        }

        StringBuilder builder = new StringBuilder(
                base.endsWith("/") ? base.substring(0, base.length() - 1) : base
        );

        if (segments == null) {
            return builder.toString();
        }

        for (Object segment : segments) {
            if (segment == null) continue;

            final String s;
            if (segment instanceof CharSequence cs) {
                s = cs.toString();
            } else if (segment instanceof Number n) {
                s = n.toString();
            } else {
                throw new IllegalArgumentException("Unsupported segment type: " + segment.getClass());
            }

            if (s.isBlank()) continue;
            builder.append("/").append(s.startsWith("/") ? s.substring(1) : s);
        }
        return builder.toString();
    }
}
