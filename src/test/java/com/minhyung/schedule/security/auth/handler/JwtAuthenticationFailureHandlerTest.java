package com.minhyung.schedule.security.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhyung.schedule.common.ApiResult;
import com.minhyung.schedule.security.auth.exception.InvalidJwtException;
import com.minhyung.schedule.security.auth.exception.TokenExpiredException;
import com.minhyung.schedule.security.exception.AuthenticationErrorCode;
import com.minhyung.schedule.testsupport.TestObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFailureHandlerTest {
    private static final ObjectMapper objectMapper = TestObjectMapper.getInstance();
    private JwtAuthenticationFailureHandler jwtAuthenticationFailureHandler;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        this.jwtAuthenticationFailureHandler = new JwtAuthenticationFailureHandler(objectMapper);
        this.request = new MockHttpServletRequest();
        this.response = new MockHttpServletResponse();
    }

    @ParameterizedTest
    @MethodSource("authException")
    void 인증_실패(AuthenticationException exception) throws IOException {
        // given
        AuthenticationErrorCode errorCode = AuthenticationErrorCode.INVALID_AUTHENTICATION;

        // when
        jwtAuthenticationFailureHandler.onAuthenticationFailure(request, response, exception);

        // then
        assertThat(response.getCharacterEncoding()).isEqualTo(StandardCharsets.UTF_8.name());
        assertThat(response.getContentType()).contains(MediaType.APPLICATION_JSON_VALUE);
        assertThat(response.getStatus()).isEqualTo(errorCode.getStatus().value());
        String body = objectMapper.writeValueAsString(ApiResult.error(errorCode));
        assertThat(response.getContentAsString()).isEqualTo(body);
    }

    @Test
    void 서버_문제로_인증_실패() throws IOException {
        // given
        AuthenticationException exception = new InternalAuthenticationServiceException("");
        AuthenticationErrorCode errorCode = AuthenticationErrorCode.AUTHENTICATION_FAILED;

        // when
        jwtAuthenticationFailureHandler.onAuthenticationFailure(request, response, exception);

        // then
        assertThat(response.getCharacterEncoding()).isEqualTo(StandardCharsets.UTF_8.name());
        assertThat(response.getContentType()).contains(MediaType.APPLICATION_JSON_VALUE);
        assertThat(response.getStatus()).isEqualTo(errorCode.getStatus().value());
        String body = objectMapper.writeValueAsString(ApiResult.error(errorCode));
        assertThat(response.getContentAsString()).isEqualTo(body);
    }

    private static Stream<AuthenticationException> authException() {
        return Stream.of(
                new DisabledException("Disabled"),      // 계정이 정지된 경우
                new TokenExpiredException("Bad credentials"),     // 모든 토큰이 만료된 경우
                new BadCredentialsException("Bad credentials"),     // 사용자 정보가 조회되지 않은 경우
                new InvalidJwtException("")     // 토큰이 유효하지 않은 경우
        );
    }
}