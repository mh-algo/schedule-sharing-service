package com.minhyung.schedule.security.jwt.scheduler;

import com.minhyung.schedule.security.jwt.repository.InMemoryTokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(InMemoryTokenStore.class) // InMemory일 때만 구동
@ConditionalOnProperty(value="jwt.cleanup.enabled", havingValue="true", matchIfMissing=true)
@RequiredArgsConstructor
public class InMemoryTokenStoreCleaner implements TokenExpiryCleaner {
    private final InMemoryTokenStore tokenStore;

    @Override
    public int removeExpired() {
        return tokenStore.removeExpired();
    }
}
