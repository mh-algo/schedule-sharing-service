package com.minhyung.schedule.security.jwt.service;

import com.minhyung.schedule.security.jwt.JwtToken;
import com.minhyung.schedule.security.jwt.repository.TokenStore;
import com.minhyung.schedule.security.principal.UserPrincipal;
import com.minhyung.schedule.security.testsupport.TestToken;
import com.minhyung.schedule.testsupport.TestClock;
import com.minhyung.schedule.testsupport.TestPrincipalBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
    private JwtService jwtService;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private TokenStore tokenStore;

    private static final Clock clock = TestClock.fixedAt("2025-08-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(jwtProvider, tokenStore, clock);
    }

    @Test
    void jwt_token_생성() {
        // given
        UserPrincipal principal = TestPrincipalBuilder.principal().build();
        String accessToken = TestToken.access(clock);
        String refreshToken = TestToken.refresh(clock);

        when(jwtProvider.generateAccessToken(principal)).thenReturn(accessToken);
        when(jwtProvider.generateRefreshToken(principal)).thenReturn(refreshToken);

        // when
        JwtToken jwtToken = jwtService.createJwtToken(principal);

        // then
        assertThat(jwtToken).isNotNull();
        assertThat(jwtToken.getAccessToken()).isEqualTo(accessToken);
        assertThat(jwtToken.getRefreshToken()).isEqualTo(refreshToken);
    }
}