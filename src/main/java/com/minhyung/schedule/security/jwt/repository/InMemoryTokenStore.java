package com.minhyung.schedule.security.jwt.repository;

import com.minhyung.schedule.security.jwt.dto.TokenData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class InMemoryTokenStore implements TokenStore {
    private final ConcurrentHashMap<String, TokenData> store = new ConcurrentHashMap<>();
    private final Clock clock;

    public InMemoryTokenStore(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void save(String sub, String refreshToken, Instant expiresAt) {
        store.put(sub, new TokenData(refreshToken, expiresAt));
    }

    @Override
    public void remove(String sub) {
        store.remove(sub);
    }

    @Override
    public boolean isInvalid(String sub, String refreshToken) {
        TokenData data = store.get(sub);
        return data == null ||
                !data.isSameToken(refreshToken) ||
                data.isExpired(getNow());
    }

    @Override
    public Optional<TokenData> find(String sub) {
        return Optional.ofNullable(store.get(sub));
    }

    public int removeExpired() {
        int before = store.size();
        store.entrySet().removeIf(entry -> entry.getValue().isExpired(getNow()));    // 만료된 토큰 제거
        int after = store.size();
        return before - after;
    }

    private Instant getNow() {
        return Instant.now(clock);
    }
}
