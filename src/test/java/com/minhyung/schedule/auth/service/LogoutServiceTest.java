package com.minhyung.schedule.auth.service;

import com.minhyung.schedule.common.exception.ApiException;
import com.minhyung.schedule.security.exception.AuthenticationErrorCode;
import com.minhyung.schedule.security.jwt.service.JwtService;
import com.minhyung.schedule.security.testsupport.TestToken;
import io.jsonwebtoken.JwtException;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {
    @InjectMocks
    private LogoutService logoutService;

    @Mock
    private JwtService jwtService;

    @Test
    void 로그아웃_성공() {
        // given
        String refreshHeader = TestToken.refreshHeader();

        // when
        logoutService.logout(refreshHeader);

        // then
    }

    @ParameterizedTest
    @MethodSource("exception")
    void 유효하지_않은_토큰의_경우_실패(String refreshHeader, RuntimeException exception) {
        // given
        doThrow(exception).when(jwtService).invalidateRefreshToken(refreshHeader, "logout");

        // when
        ThrowingCallable action = () -> logoutService.logout(refreshHeader);

        // then
        AuthenticationErrorCode errorCode = AuthenticationErrorCode.INVALID_AUTHENTICATION;
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(action)
                .withMessage(errorCode.getMessage());
    }

    private static Stream<Arguments> exception() {
        return Stream.of(
                Arguments.of(TestToken.refresh(), new JwtException("")),
                Arguments.of("", new IllegalArgumentException(""))
        );
    }
}