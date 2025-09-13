package com.minhyung.schedule.security.jwt.scheduler;

import com.minhyung.schedule.security.jwt.repository.InMemoryTokenBlackList;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(InMemoryTokenBlackList.class) // InMemory일 때만 구동
@ConditionalOnProperty(value="jwt.cleanup.enabled", havingValue="true", matchIfMissing=true)
@RequiredArgsConstructor
public class InMemoryTokenBlackListCleaner implements TokenExpiryCleaner {
    private final InMemoryTokenBlackList tokenBlackList;

    @Override
    public int removeExpired() {
        return tokenBlackList.removeExpired();
    }
}
