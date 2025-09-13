package com.minhyung.schedule.security.auth;

import com.minhyung.schedule.security.auth.exception.AccessTokenExpiredException;
import com.minhyung.schedule.security.auth.exception.InvalidJwtException;
import com.minhyung.schedule.security.jwt.JwtToken;
import com.minhyung.schedule.security.jwt.service.JwtService;
import com.minhyung.schedule.security.principal.UserPrincipalMapper;
import com.minhyung.schedule.security.testsupport.TestToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtException;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationProviderTest {
    @InjectMocks
    private JwtAuthenticationProvider jwtAuthenticationProvider;

    @Mock
    private JwtService jwtService;

    @Mock
    private Claims claims;

    private static final String SUB = "1";
    private static final JwtToken TOKENS = TestToken.tokens(SUB);

    @Test
    void access_token_인증_성공() {
        // given
        JwtAuthenticationToken unauthenticated = JwtAuthenticationToken.unauthenticated(TOKENS);

        when(jwtService.verifyAccessToken(TOKENS.getAccessToken())).thenReturn(claims);
        when(claims.getSubject()).thenReturn(SUB);
        when(claims.get("verified", Boolean.class)).thenReturn(true);

        // when
        Authentication authenticate = jwtAuthenticationProvider.authenticate(unauthenticated);

        // then
        assertThat(authenticate).isNotNull();
        assertThat(authenticate.getPrincipal()).isEqualTo(UserPrincipalMapper.from(claims));
        assertThat(authenticate.getCredentials()).isEqualTo(TOKENS);
        assertThat(authenticate.isAuthenticated()).isTrue();
        Optional<? extends GrantedAuthority> authority = authenticate.getAuthorities().stream().findFirst();
        assertThat(authority).isNotEmpty();
        assertThat(authority.get().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    void access_token이_만료된_경우() {
        // given
        JwtAuthenticationToken unauthenticated = JwtAuthenticationToken.unauthenticated(TOKENS);

        when(jwtService.verifyAccessToken(TOKENS.getAccessToken()))
                .thenThrow(new ExpiredJwtException(mock(Header.class), claims, ""));

        // when
        ThrowingCallable action = () -> jwtAuthenticationProvider.authenticate(unauthenticated);

        // then
        assertThatExceptionOfType(AccessTokenExpiredException.class)
                .isThrownBy(action)
                .withMessage("Expired Access token");
    }

    @Test
    void access_token이_유효하지_않은_경우() {
        // given
        JwtAuthenticationToken unauthenticated = JwtAuthenticationToken.unauthenticated(TOKENS);

        when(jwtService.verifyAccessToken(TOKENS.getAccessToken())).thenThrow(new JwtException(""));

        // when
        ThrowingCallable action = () -> jwtAuthenticationProvider.authenticate(unauthenticated);

        // then
        assertThatExceptionOfType(InvalidJwtException.class).isThrownBy(action);
    }
}