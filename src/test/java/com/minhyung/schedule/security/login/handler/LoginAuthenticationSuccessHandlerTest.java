package com.minhyung.schedule.security.login.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhyung.schedule.common.ApiResult;
import com.minhyung.schedule.security.jwt.JwtHeader;
import com.minhyung.schedule.security.jwt.JwtToken;
import com.minhyung.schedule.security.jwt.service.JwtService;
import com.minhyung.schedule.security.principal.UserPrincipal;
import com.minhyung.schedule.security.testsupport.TestLoginAuthenticationToken;
import com.minhyung.schedule.security.testsupport.TestToken;
import com.minhyung.schedule.testsupport.TestObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginAuthenticationSuccessHandlerTest {
    private static final ObjectMapper objectMapper = TestObjectMapper.getInstance();

    @InjectMocks
    private LoginAuthenticationSuccessHandler successHandler;

    @Mock
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        successHandler.setObjectMapper(objectMapper);
    }

    @Test
    void 로그인_인증_성공() throws IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        UsernamePasswordAuthenticationToken authentication = TestLoginAuthenticationToken.authenticated();
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        JwtToken jwtToken = TestToken.tokens();

        when(jwtService.createJwtToken(principal)).thenReturn(jwtToken);

        // when
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(response.getHeader(JwtHeader.ACCESS_TOKEN)).isEqualTo(jwtToken.getAuthHeader());
        assertThat(response.getHeader(JwtHeader.REFRESH_TOKEN)).isEqualTo(jwtToken.getRefreshHeader());
        assertThat(response.getCharacterEncoding()).isEqualTo(StandardCharsets.UTF_8.name());
        assertThat(response.getContentType()).contains(MediaType.APPLICATION_JSON_VALUE);
        String expectedBody = objectMapper.writeValueAsString(ApiResult.success("로그인 성공"));
        assertThat(response.getContentAsString()).isEqualTo(expectedBody);
    }

    @Test
    void principal의_타입이_UserPrincipal가_아닌_경우() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = mock(Authentication.class);

        when(authentication.getPrincipal()).thenReturn(
                User.builder().username("username").password("password123!").build());

        // when
        ThrowingCallable action = () -> successHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        assertThatExceptionOfType(InternalAuthenticationServiceException.class)
                .isThrownBy(action)
                .withMessage("Unexpected principal type");
    }
}