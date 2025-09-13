package com.minhyung.schedule.security.jwt.service;

import com.minhyung.schedule.auth.domain.UserStatus;
import com.minhyung.schedule.auth.dto.UserStatusDto;
import com.minhyung.schedule.auth.service.UserService;
import com.minhyung.schedule.security.jwt.JwtToken;
import com.minhyung.schedule.security.jwt.dto.IssuedToken;
import com.minhyung.schedule.security.jwt.dto.TokenData;
import com.minhyung.schedule.security.jwt.exception.DisabledAccountException;
import com.minhyung.schedule.security.jwt.repository.TokenBlackList;
import com.minhyung.schedule.security.jwt.repository.TokenStore;
import com.minhyung.schedule.security.principal.UserPrincipal;
import com.minhyung.schedule.security.testsupport.TestToken;
import com.minhyung.schedule.testsupport.TestClock;
import com.minhyung.schedule.testsupport.TestPrincipalBuilder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
    private JwtService jwtService;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private TokenStore tokenStore;

    @Mock
    private TokenBlackList tokenBlackList;

    @Mock
    private UserService userService;

    private static final Clock CLOCK = TestClock.now();       // 현재 시간
    private static final Instant NOW = Instant.now(CLOCK);
    private static final UserPrincipal PRINCIPAL = createPrincipal();
    private static final String SUB = PRINCIPAL.id().toString();
    private static final Clock CREATED_AT = Clock.offset(CLOCK, Duration.ofDays(-1));      // 요청받은 토큰의 발급된 시간

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(jwtProvider, tokenStore, tokenBlackList, CLOCK, userService);
    }

    private static UserPrincipal createPrincipal() {
        return TestPrincipalBuilder.principal().build();
    }

    private static IssuedToken issueRefresh(String sub, String refreshToken) {
        return new IssuedToken(sub, refreshToken, NOW.plus(Duration.ofDays(7)));
    }

    private static IssuedToken issueAccess(String sub, String accessToken) {
        return new IssuedToken(sub, accessToken, NOW.plus(Duration.ofHours(1)));
    }

    @Test
    void jwt_token_생성() {
        // given
        String accessToken = TestToken.access(SUB, CLOCK);
        String refreshToken = TestToken.refresh(SUB, CLOCK);

        when(jwtProvider.issueAccess(PRINCIPAL, NOW)).thenReturn(issueAccess(SUB, accessToken));
        when(jwtProvider.issueRefresh(PRINCIPAL, NOW)).thenReturn(issueRefresh(SUB, refreshToken));

        // when
        JwtToken jwtToken = jwtService.issueJwtToken(PRINCIPAL);

        // then
        assertThat(jwtToken).isNotNull();
        assertThat(jwtToken.getAccessToken()).isEqualTo(accessToken);
        assertThat(jwtToken.getRefreshToken()).isEqualTo(refreshToken);
    }


    @Test
    void jwt_token_재발급() {
        // given
        Long id = Long.valueOf(SUB);
        String refreshToken = TestToken.refresh(SUB, CREATED_AT);
        Claims claims = mock(Claims.class);
        TokenData tokenData = new TokenData(refreshToken, Instant.now(CREATED_AT).plus(Duration.ofDays(7)));

        when(jwtProvider.parseClaims(refreshToken)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(SUB);
        when(tokenStore.find(SUB)).thenReturn(Optional.of(tokenData));
        when(userService.getUserStatusDto(id)).thenReturn(new UserStatusDto(id, UserStatus.ACTIVE));

        String reissueAccessToken = TestToken.access(SUB, CLOCK);
        String reissueRefreshToken = TestToken.refresh(SUB, CLOCK);
        when(jwtProvider.issueAccess(PRINCIPAL, NOW)).thenReturn(issueAccess(SUB, reissueAccessToken));
        when(jwtProvider.issueRefresh(PRINCIPAL, NOW)).thenReturn(issueRefresh(SUB, reissueRefreshToken));

        // when
        JwtToken jwtToken = jwtService.reissueJwtToken(refreshToken);

        // then
        assertThat(jwtToken).isNotNull();
        assertThat(jwtToken.getAccessToken()).isEqualTo(reissueAccessToken);
        assertThat(jwtToken.getRefreshToken()).isEqualTo(reissueRefreshToken);
    }

    @Test
    void jwt_token_재발급_시_유효하지_않은_jwt인_경우() {
        // given
        String refreshToken = TestToken.refresh(SUB, CREATED_AT);

        when(jwtProvider.parseClaims(refreshToken)).thenThrow(new JwtException(""));

        // when
        ThrowingCallable action = () -> jwtService.reissueJwtToken(refreshToken);

        // then
        assertThatExceptionOfType(JwtException.class)
                .isThrownBy(action);
    }

    @Test
    void jwt_token_재발급_시_해당_토큰이_서버에서_발급된_토큰이_아닌_경우() {
        // given
        String refreshToken = TestToken.refresh(SUB, CREATED_AT);
        Claims claims = mock(Claims.class);

        when(jwtProvider.parseClaims(refreshToken)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(SUB);
        when(tokenStore.isInvalid(SUB, refreshToken)).thenReturn(true);

        // when
        ThrowingCallable action = () -> jwtService.reissueJwtToken(refreshToken);

        // then
        assertThatExceptionOfType(JwtException.class)
                .isThrownBy(action)
                .withMessage("Invalid JWT");
    }

    @Test
    void jwt_token_재발급_시_해당_토큰이_블랙리스트에_등록된_토큰인_경우() {
        // given
        String refreshToken = TestToken.refresh(SUB, CREATED_AT);
        Claims claims = mock(Claims.class);

        when(jwtProvider.parseClaims(refreshToken)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(SUB);
        when(tokenBlackList.isBlackListed(refreshToken)).thenReturn(true);

        // when
        ThrowingCallable action = () -> jwtService.reissueJwtToken(refreshToken);

        // then
        assertThatExceptionOfType(JwtException.class)
                .isThrownBy(action)
                .withMessage("Invalid JWT");
    }

    @Test
    void jwt_token_재발급_시_해당_계정이_정지된_경우() {
        // given
        Long id = Long.valueOf(SUB);
        String refreshToken = TestToken.refresh(SUB, CREATED_AT);
        Claims claims = mock(Claims.class);
        TokenData tokenData = new TokenData(refreshToken, Instant.now(CREATED_AT).plus(Duration.ofDays(7)));

        when(jwtProvider.parseClaims(refreshToken)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(SUB);
        when(tokenStore.find(SUB)).thenReturn(Optional.of(tokenData));
        when(userService.getUserStatusDto(id)).thenReturn(new UserStatusDto(id, UserStatus.SUSPENDED));

        // when
        ThrowingCallable action = () -> jwtService.reissueJwtToken(refreshToken);

        // then
        assertThatExceptionOfType(DisabledAccountException.class)
                .isThrownBy(action)
                .withMessage("Account is disabled");
    }

    @Test
    void 유효한_access_token일_경우() {
        // given
        String accessToken = TestToken.access(SUB, CLOCK);
        Claims claims = mock(Claims.class);

        when(jwtProvider.parseClaims(accessToken)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(SUB);

        // when
        Claims result = jwtService.verifyAccessToken(accessToken);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSubject()).isEqualTo(SUB);
    }

    @Test
    void 유효하지_않은_access_token일_경우() {
        // given
        String accessToken = TestToken.access(SUB, CLOCK);

        when(jwtProvider.parseClaims(accessToken)).thenThrow(new JwtException(""));

        // when
        ThrowingCallable action = () -> jwtService.verifyAccessToken(accessToken);

        // then
        assertThatExceptionOfType(JwtException.class)
                .isThrownBy(action);
    }

    @Test
    void access_token이_empty인_경우() {
        // given
        String accessToken = "";

        when(jwtProvider.parseClaims(accessToken)).thenThrow(new IllegalArgumentException(""));

        // when
        ThrowingCallable action = () -> jwtService.verifyAccessToken(accessToken);

        // then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(action);
    }

    @Test
    void 유효한_refresh_token일_경우() {
        // given
        String refreshToken = TestToken.refresh(SUB, CLOCK);
        Claims claims = mock(Claims.class);

        when(jwtProvider.parseClaims(refreshToken)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(SUB);

        // when
        Claims result = jwtService.verifyRefreshToken(refreshToken);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSubject()).isEqualTo(SUB);
    }

    @Test
    void 유효하지_않은_refresh_token일_경우() {
        // given
        String refreshToken = TestToken.refresh(SUB, CLOCK);

        when(jwtProvider.parseClaims(refreshToken)).thenThrow(new JwtException(""));

        // when
        ThrowingCallable action = () -> jwtService.verifyRefreshToken(refreshToken);

        // then
        assertThatThrownBy(action).isInstanceOf(JwtException.class);
    }

    @Test
    void refresh_token이_empty일_경우() {
        // given
        String refreshToken = "";

        when(jwtProvider.parseClaims(refreshToken)).thenThrow(new IllegalArgumentException(""));

        // when
        ThrowingCallable action = () -> jwtService.verifyRefreshToken(refreshToken);

        // then
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refresh_token이_서버에서_발급된_토큰이_아닌_경우() {
        // given
        String refreshToken = TestToken.refresh(SUB, CREATED_AT);
        Claims claims = mock(Claims.class);

        when(jwtProvider.parseClaims(refreshToken)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(SUB);
        when(tokenStore.isInvalid(SUB, refreshToken)).thenReturn(true);

        // when
        ThrowingCallable action = () -> jwtService.verifyRefreshToken(refreshToken);

        // then
        assertThatExceptionOfType(JwtException.class)
                .isThrownBy(action)
                .withMessage("Invalid JWT");
    }

    @Test
    void refresh_token이_블랙리스트에_등록된_토큰인_경우() {
        // given
        String refreshToken = TestToken.refresh(SUB, CREATED_AT);
        Claims claims = mock(Claims.class);

        when(jwtProvider.parseClaims(refreshToken)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(SUB);
        when(tokenBlackList.isBlackListed(refreshToken)).thenReturn(true);

        // when
        ThrowingCallable action = () -> jwtService.verifyRefreshToken(refreshToken);

        // then
        assertThatExceptionOfType(JwtException.class)
                .isThrownBy(action)
                .withMessage("Invalid JWT");
    }
}