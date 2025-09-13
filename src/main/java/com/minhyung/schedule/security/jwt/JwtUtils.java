package com.minhyung.schedule.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

public final class JwtUtils {
    public static String encode(String subject, Map<String, Object> claims, String secretKey, Instant now, Instant expiresAt) {
        Date iat = Date.from(now);
        Date exp = Date.from(expiresAt);

        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(iat)
                .expiration(exp)
                .signWith(createSigningKey(secretKey))
                .compact();
    }

    public static String encode(String subject, String secretKey, Instant now, Instant expiresAt) {
        Date iat = Date.from(now);
        Date exp = Date.from(expiresAt);

        return Jwts.builder()
                .subject(subject)
                .issuedAt(iat)
                .expiration(exp)
                .signWith(createSigningKey(secretKey))
                .compact();
    }

    public static Claims decode(String token, String secretKey) throws JwtException {
        return Jwts.parser()
                .verifyWith(createSigningKey(secretKey))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private static SecretKey createSigningKey(String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}