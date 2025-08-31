package com.minhyung.schedule.security.jwt.repository;

import com.minhyung.schedule.security.jwt.dto.BlackListedToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class InMemoryTokenBlackList implements TokenBlackList {
    private final ConcurrentHashMap<String, BlackListedToken> blackList = new ConcurrentHashMap<>();

    @Override
    public void save(String token, String reason, Instant expiresAt) {
        BlackListedToken existingToken = blackList.putIfAbsent(
                token, new BlackListedToken(reason, expiresAt));
        if (existingToken != null) {
            log.warn("This token is already blacklisted.");
        }
    }

    @Override
    public boolean isBlackListed(String token) {
        return blackList.containsKey(token);
    }
}
