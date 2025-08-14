package com.minhyung.schedule.security.jwt;

import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

@EqualsAndHashCode
public class JwtToken {
    private static final String PREFIX = "Bearer ";

    @NonNull
    private final String access;

    @NonNull
    private final String refresh;

    private JwtToken(String access, String refresh) {
        this.access = access != null ? access.trim() : "";
        this.refresh = refresh != null ? refresh.trim() : "";
    }

    public static JwtToken ofBearer(String authHeader, String refreshHeader) {
        return new JwtToken(authHeader, refreshHeader);
    }

    public static JwtToken ofRaw(String accessToken, String refreshToken) {
        return new JwtToken(accessToken, refreshToken);
    }

    public String getAuthHeader() {
        return ensureBearerPrefix(access);
    }

    public String getRefreshHeader() {
        return ensureBearerPrefix(refresh);
    }

    private String ensureBearerPrefix(String token) {
        return (token.startsWith(PREFIX) || token.isEmpty())
                ? token
                : PREFIX + token;
    }

    public String getAccessToken() {
        return removeBearerPrefix(access);
    }

    public String getRefreshToken() {
        return removeBearerPrefix(refresh);
    }

    private String removeBearerPrefix(String token) {
        return token.startsWith(PREFIX)
                ? token.substring(PREFIX.length())
                : token;
    }
}
