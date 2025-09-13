package com.minhyung.schedule.security.auth.handler;

import com.minhyung.schedule.security.auth.JwtAuthenticationToken;
import com.minhyung.schedule.security.jwt.JwtHeader;
import com.minhyung.schedule.security.jwt.JwtToken;
import com.minhyung.schedule.security.principal.UserPrincipal;
import com.minhyung.schedule.security.testsupport.TestToken;
import com.minhyung.schedule.testsupport.TestRequestBuilder;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationSuccessHandlerTest {
    private JwtAuthenticationSuccessHandler jwtAuthenticationSuccessHandler;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private static final String AUTH_REQUIRED_PATH = "/";

    @BeforeEach
    void setUp() {
        this.jwtAuthenticationSuccessHandler = new JwtAuthenticationSuccessHandler();
        this.request = TestRequestBuilder.json()
                .post(AUTH_REQUIRED_PATH)
                .build();
        this.response = new MockHttpServletResponse();
    }

    @Test
    void 인증_성공() {
        // given
        JwtToken tokens = TestToken.headers();
        request.addHeader(JwtHeader.ACCESS_TOKEN, tokens.getAuthHeader());
        request.addHeader(JwtHeader.REFRESH_TOKEN, tokens.getRefreshHeader());

        JwtAuthenticationToken authenticated = JwtAuthenticationToken.authenticated(
                mock(UserPrincipal.class), tokens, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        // when
        jwtAuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authenticated);

        // then
        assertThat(response.getHeader(JwtHeader.ACCESS_TOKEN)).isEqualTo(tokens.getAuthHeader());
        assertThat(response.getHeader(JwtHeader.REFRESH_TOKEN)).isEqualTo(tokens.getRefreshHeader());
    }

    @Test
    void 객체의_타입이_일치하지_않는_경우() {
        // given
        Authentication authenticated = UsernamePasswordAuthenticationToken.authenticated(
                mock(Object.class), mock(Object.class), List.of(new SimpleGrantedAuthority("ROLE_USER")));

        // when
        ThrowingCallable action = () -> jwtAuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authenticated);

        // then
        assertThatExceptionOfType(InternalAuthenticationServiceException.class)
                .isThrownBy(action)
                .withMessage("Unexpected credentials type");
    }
}