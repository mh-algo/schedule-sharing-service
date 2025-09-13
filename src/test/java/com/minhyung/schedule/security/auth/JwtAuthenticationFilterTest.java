package com.minhyung.schedule.security.auth;

import com.minhyung.schedule.common.ApiPathsUtils;
import com.minhyung.schedule.security.auth.exception.AccessTokenExpiredException;
import com.minhyung.schedule.security.auth.exception.InvalidJwtException;
import com.minhyung.schedule.security.auth.exception.TokenExpiredException;
import com.minhyung.schedule.security.jwt.JwtHeader;
import com.minhyung.schedule.security.jwt.JwtToken;
import com.minhyung.schedule.security.jwt.service.JwtService;
import com.minhyung.schedule.security.principal.UserPrincipal;
import com.minhyung.schedule.security.testsupport.TestToken;
import com.minhyung.schedule.testsupport.TestRequestBuilder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AuthenticationSuccessHandler successHandler;

    @Mock
    private AuthenticationFailureHandler failureHandler;

    @Mock
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        this.jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService);
        this.jwtAuthenticationFilter.setAuthenticationManager(authenticationManager);
        this.jwtAuthenticationFilter.setAuthenticationSuccessHandler(successHandler);
        this.jwtAuthenticationFilter.setAuthenticationFailureHandler(failureHandler);
    }

    private static final String AUTH_REQUIRED_PATH = "/";

    @Test
    void 인증_성공() throws ServletException, IOException {
        // given
        JwtToken tokens = TestToken.headers();
        Map<String, String> headers = createHeaders(tokens.getAuthHeader(), tokens.getRefreshHeader());
        MockHttpServletRequest request = createRequest(headers, AUTH_REQUIRED_PATH);
        MockHttpServletResponse response = new MockHttpServletResponse();
        JwtAuthenticationToken authenticated = JwtAuthenticationToken.authenticated(
                mock(UserPrincipal.class), tokens, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(authenticationManager.authenticate(JwtAuthenticationToken.unauthenticated(tokens))).thenReturn(authenticated);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, mock(FilterChain.class));

        // then
        verify(authenticationManager, only()).authenticate(any());
        verify(successHandler, never()).onAuthenticationSuccess(any(), any(), any());
        verify(failureHandler, never()).onAuthenticationFailure(any(), any(), any());
    }

    @Test
    void 토큰_없이_인증이_필요하지_않은_경로로_요청한_경우() throws ServletException, IOException {
        // given
        String permittedPath = ApiPathsUtils.auth("login");
        Map<String, String> headers = createHeaders("", "");
        MockHttpServletRequest request = createRequest(headers, permittedPath);
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, mock(FilterChain.class));

        // then
        verify(authenticationManager, never()).authenticate(any());
        verify(successHandler, never()).onAuthenticationSuccess(any(), any(), any());
        verify(failureHandler, never()).onAuthenticationFailure(any(), any(), any());
    }

    @Test
    void 요청_토큰의_prefix가_일치하지_않는_경우() throws ServletException, IOException {
        // given
        JwtToken tokens = TestToken.tokens();
        Map<String, String> headers = createHeaders(tokens.getAccessToken(), tokens.getRefreshToken());
        MockHttpServletRequest request = createRequest(headers, AUTH_REQUIRED_PATH);
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, mock(FilterChain.class));

        // then
        verify(authenticationManager, never()).authenticate(any());
        verify(successHandler, never()).onAuthenticationSuccess(any(), any(), any());
        verify(failureHandler, only()).onAuthenticationFailure(any(), any(), any());
    }

    @Test
    void access_token_만료되어서_토큰_재발급() throws ServletException, IOException {
        // given
        JwtToken tokens = TestToken.headers();
        Map<String, String> headers = createHeaders(tokens.getAuthHeader(), tokens.getRefreshHeader());
        MockHttpServletRequest request = createRequest(headers, AUTH_REQUIRED_PATH);
        MockHttpServletResponse response = new MockHttpServletResponse();
        JwtToken reissued = TestToken.tokens();
        JwtAuthenticationToken authenticated = JwtAuthenticationToken.authenticated(
                mock(UserPrincipal.class), reissued, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(authenticationManager.authenticate(JwtAuthenticationToken.unauthenticated(tokens))).
                thenThrow(new AccessTokenExpiredException("")); //  access token 만료
        when(jwtService.reissueJwtToken(tokens.getRefreshToken())).thenReturn(reissued);    // 토큰 재발급
        when(authenticationManager.authenticate(JwtAuthenticationToken.unauthenticated(reissued))).
                thenReturn(authenticated);  // 재발급된 토큰으로 재인증

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, mock(FilterChain.class));

        // then
        verify(authenticationManager, times(2)).authenticate(any());
        verify(successHandler, times(1)).onAuthenticationSuccess(any(), any(), any());
        verify(failureHandler, never()).onAuthenticationFailure(any(), any(), any());
    }

    @Test
    void access_token없이_refresh_token만_있는_경우_토큰_재발급() throws ServletException, IOException {
        // given
        String refreshHeader = TestToken.refreshHeader();
        JwtToken tokens = TestToken.headers("", refreshHeader);
        Map<String, String> headers = createHeaders("", refreshHeader);
        MockHttpServletRequest request = createRequest(headers, AUTH_REQUIRED_PATH);
        MockHttpServletResponse response = new MockHttpServletResponse();
        JwtToken reissued = TestToken.tokens();
        JwtAuthenticationToken authenticated = JwtAuthenticationToken.authenticated(
                mock(UserPrincipal.class), reissued, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(authenticationManager.authenticate(JwtAuthenticationToken.unauthenticated(tokens))).
                thenThrow(new InvalidJwtException(""));
        when(jwtService.reissueJwtToken(tokens.getRefreshToken())).thenReturn(reissued);    // 토큰 재발급
        when(authenticationManager.authenticate(JwtAuthenticationToken.unauthenticated(reissued))).
                thenReturn(authenticated);  // 재발급된 토큰으로 재인증

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, mock(FilterChain.class));

        // then
        verify(authenticationManager, times(2)).authenticate(any());
        verify(successHandler, times(1)).onAuthenticationSuccess(any(), any(), any());
        verify(failureHandler, never()).onAuthenticationFailure(any(), any(), any());
    }

    @ParameterizedTest
    @MethodSource("reissueException")
    void 토큰_재발급_실패(AuthenticationException exception) throws ServletException, IOException {
        // given
        JwtToken tokens = TestToken.headers();
        Map<String, String> headers = createHeaders(tokens.getAuthHeader(), tokens.getRefreshHeader());
        MockHttpServletRequest request = createRequest(headers, AUTH_REQUIRED_PATH);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(authenticationManager.authenticate(JwtAuthenticationToken.unauthenticated(tokens))).
                thenThrow(new AccessTokenExpiredException("")); //  access token 만료
        when(jwtService.reissueJwtToken(tokens.getRefreshToken())).thenThrow(exception);    // 토큰 재발급 실패

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, mock(FilterChain.class));

        // then
        verify(authenticationManager, times(1)).authenticate(any());
        verify(successHandler, never()).onAuthenticationSuccess(any(), any(), any());
        verify(failureHandler, times(1)).onAuthenticationFailure(any(), any(), any());
    }

    private static Stream<AuthenticationException> reissueException() {
        return Stream.of(
                new DisabledException("Disabled"),      // 계정이 정지된 경우
                new BadCredentialsException("Bad credentials"),     // 사용자 정보 조회 실패
                new TokenExpiredException("Expired JWT token"),     // 만료된 refresh 토큰
                new InvalidJwtException("")     // 유효하지 않은 토큰
        );
    }

    private static MockHttpServletRequest createRequest(Map<String, String> headers, String path) {
        return TestRequestBuilder.json()
                .headers(headers)
                .post(path)
                .build();
    }

    private static Map<String, String> createHeaders(String authHeader, String refreshHeader) {
        return Map.of(
                JwtHeader.ACCESS_TOKEN, authHeader,
                JwtHeader.REFRESH_TOKEN, refreshHeader
        );
    }
}