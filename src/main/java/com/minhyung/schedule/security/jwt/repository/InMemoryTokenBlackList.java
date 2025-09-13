package com.minhyung.schedule.security.jwt.repository;

import com.minhyung.schedule.security.jwt.dto.BlackListedToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class InMemoryTokenBlackList implements TokenBlackList {
    private final ConcurrentHashMap<String, BlackListedToken> blackList = new ConcurrentHashMap<>();
    private final Clock clock;

    public InMemoryTokenBlackList(Clock clock) {
        this.clock = clock;
    }

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

    public int removeExpired() {
        int before = blackList.size();
        blackList.entrySet().removeIf(entry -> entry.getValue().isExpired(getNow()));    // 만료된 토큰 제거
        int after = blackList.size();
        return before - after;
    }

    private Instant getNow() {
        return Instant.now(clock);
    }
}
