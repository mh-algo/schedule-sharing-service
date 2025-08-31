package com.minhyung.schedule.security.jwt.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhyung.schedule.security.jwt.dto.IssuedToken;
import com.minhyung.schedule.security.principal.UserPrincipal;
import com.minhyung.schedule.security.testsupport.TestToken;
import com.minhyung.schedule.testsupport.TestClock;
import com.minhyung.schedule.testsupport.TestObjectMapper;
import com.minhyung.schedule.testsupport.TestPrincipalBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtProviderTest {
    private static final String SECRET_KEY = TestToken.SECRET_KEY;
    private static final long ACCESS_TOKEN_TTL_MS = 3600000L;
    private static final long REFRESH_TOKEN_TTL_MS = 604800000L;
    private static final ObjectMapper objectMapper = TestObjectMapper.getInstance();
    private static final Clock CLOCK = TestClock.fixedAt("2025-08-01T00:00:00Z");
    private static final Instant NOW = Instant.now(CLOCK);

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(objectMapper);
        ReflectionTestUtils.setField(jwtProvider, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtProvider, "accessTokenTTLMs", ACCESS_TOKEN_TTL_MS);
        ReflectionTestUtils.setField(jwtProvider, "refreshTokenTTLMs", REFRESH_TOKEN_TTL_MS);
    }

    private static UserPrincipal createPrincipal() {
        return TestPrincipalBuilder.principal().build();
    }

    @Test
    void access_token_생성() {
        // given
        UserPrincipal principal = createPrincipal();
        String sub = principal.id().toString();
        String accessToken = TestToken.access(CLOCK);
        Instant expiresAt = NOW.plusMillis(ACCESS_TOKEN_TTL_MS);

        // when
        IssuedToken result = jwtProvider.issueAccess(principal, NOW);

        // then
        assertThat(result.sub()).isEqualTo(sub);
        assertThat(result.token()).isEqualTo(accessToken);
        assertThat(result.expiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void refresh_token_생성() {
        // given
        UserPrincipal principal = createPrincipal();
        String sub = principal.id().toString();
        String refreshToken = TestToken.refresh(CLOCK);
        Instant expiresAt = NOW.plusMillis(REFRESH_TOKEN_TTL_MS);

        // when
        IssuedToken result = jwtProvider.issueRefresh(principal, NOW);

        // then
        assertThat(result.sub()).isEqualTo(sub);
        assertThat(result.token()).isEqualTo(refreshToken);
        assertThat(result.expiresAt()).isEqualTo(expiresAt);
    }
}