package com.minhyung.schedule.security.login.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhyung.schedule.common.ApiResult;
import com.minhyung.schedule.common.exception.ErrorCode;
import com.minhyung.schedule.security.exception.AuthenticationErrorCode;
import com.minhyung.schedule.security.exception.MethodNotAllowedException;
import com.minhyung.schedule.security.login.exception.InvalidJsonFormatException;
import com.minhyung.schedule.security.login.exception.InvalidJsonPropertyException;
import com.minhyung.schedule.security.login.exception.LoginErrorCode;
import com.minhyung.schedule.testsupport.TestObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class LoginAuthenticationFailureHandlerTest {
    private static final ObjectMapper objectMapper = TestObjectMapper.getInstance();

    private LoginAuthenticationFailureHandler failureHandler;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        failureHandler = new LoginAuthenticationFailureHandler();
        failureHandler.setObjectMapper(objectMapper);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @ParameterizedTest
    @MethodSource("exceptionToCode")
    void 로그인_인증_실패(AuthenticationException exception, ErrorCode errorCode) throws IOException {
        // given

        // when
        failureHandler.onAuthenticationFailure(request, response, exception);

        // then
        assertThat(response.getStatus()).isEqualTo(errorCode.getStatus().value());
        assertThat(response.getCharacterEncoding()).isEqualTo(StandardCharsets.UTF_8.name());
        assertThat(response.getContentType()).contains(MediaType.APPLICATION_JSON_VALUE);
        String body = objectMapper.writeValueAsString(createPayload(exception, errorCode));
        assertThat(response.getContentAsString()).isEqualTo(body);
    }

    @Test
    void post_요청이_아닌_경우() throws IOException {
        // given
        MethodNotAllowedException exception = new MethodNotAllowedException("");

        // when
        failureHandler.onAuthenticationFailure(request, response, exception);

        // then
        ErrorCode errorCode = AuthenticationErrorCode.METHOD_NOT_ALLOWED;
        assertThat(response.getStatus()).isEqualTo(errorCode.getStatus().value());
        assertThat(response.getCharacterEncoding()).isEqualTo(StandardCharsets.UTF_8.name());
        assertThat(response.getContentType()).contains(MediaType.APPLICATION_JSON_VALUE);
        assertThat(response.getHeader("Allow")).isEqualTo(HttpMethod.POST.name());
        String body = objectMapper.writeValueAsString(createPayload(errorCode));
        assertThat(response.getContentAsString()).isEqualTo(body);
    }

    private static Stream<Arguments> exceptionToCode() {
        return Stream.of(
                Arguments.of(new BadCredentialsException(""), LoginErrorCode.INVALID_CREDENTIALS),    // 로그인 정보 불일치
                Arguments.of(new DisabledException(""),    LoginErrorCode.ACCOUNT_SUSPENDED),     // 계정 정지
                Arguments.of(new InvalidJsonPropertyException("", null, "usernam"), AuthenticationErrorCode.INVALID_JSON_PROPERTY),    // 잘못된 json property
                Arguments.of(new InvalidJsonFormatException(""), AuthenticationErrorCode.INVALID_JSON_FORMAT),    // 잘못된 요청 형식(json parsing 실패)
                Arguments.of(new AuthenticationServiceException(""), LoginErrorCode.LOGIN_FAILED),
                Arguments.of(new InternalAuthenticationServiceException(""), LoginErrorCode.LOGIN_FAILED)
        );
    }

    private static ApiResult<Void> createPayload(ErrorCode errorCode) {
        return createPayload(null, errorCode);
    }

    private static ApiResult<Void> createPayload(AuthenticationException exception, ErrorCode errorCode) {
        if (exception instanceof InvalidJsonPropertyException e) {
            String message = String.format(errorCode.getMessage(), e.getPropertyName());
            return ApiResult.error(errorCode, message);
        }
        return ApiResult.error(errorCode, errorCode.getMessage());
    }
}