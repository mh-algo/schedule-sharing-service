package com.minhyung.schedule.security.login;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhyung.schedule.common.ApiPathsUtils;
import com.minhyung.schedule.security.exception.MethodNotAllowedException;
import com.minhyung.schedule.security.login.exception.InvalidJsonFormatException;
import com.minhyung.schedule.security.login.exception.InvalidJsonPropertyException;
import com.minhyung.schedule.security.login.handler.LoginAuthenticationFailureHandler;
import com.minhyung.schedule.security.login.handler.LoginAuthenticationSuccessHandler;
import com.minhyung.schedule.security.testsupport.TestLoginAuthenticationToken;
import com.minhyung.schedule.testsupport.TestObjectMapper;
import com.minhyung.schedule.testsupport.TestRequestBuilder;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiLoginFilterTest {
    private ApiLoginFilter apiLoginFilter;

    @Mock
    private ProviderManager providerManager;

    @Mock
    private LoginAuthenticationSuccessHandler successHandler;

    @Mock
    private LoginAuthenticationFailureHandler failureHandler;

    private static final ObjectMapper objectMapper = TestObjectMapper.getInstance();
    private static final String REQUEST_URI = ApiPathsUtils.auth("login");

    @BeforeEach
    void setUp() {
        apiLoginFilter = new ApiLoginFilter(objectMapper);
        apiLoginFilter.setAuthenticationManager(providerManager);
        apiLoginFilter.setRequiresAuthenticationRequestMatcher(
                PathPatternRequestMatcher.withDefaults().matcher(REQUEST_URI));
        apiLoginFilter.setAuthenticationSuccessHandler(successHandler);
        apiLoginFilter.setAuthenticationFailureHandler(failureHandler);
    }

    @Test
    void 로그인_성공() throws IOException {
        // given
        String username = "username";
        String password = "password123!";

        MockHttpServletRequest request = createRequest(username, password);
        MockHttpServletResponse response = new MockHttpServletResponse();

        UsernamePasswordAuthenticationToken unauthenticated = TestLoginAuthenticationToken.unauthenticated(username, password);
        unauthenticated.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        UsernamePasswordAuthenticationToken authenticated = TestLoginAuthenticationToken.authenticated();

        when(providerManager.authenticate(unauthenticated)).thenReturn(authenticated);

        // when
        Authentication authentication = apiLoginFilter.attemptAuthentication(request, response);

        // then
        assertThat(authentication).isInstanceOf(UsernamePasswordAuthenticationToken.class);
    }

    @Test
    void post_요청이_아닌_경우() {
        // given
        String username = "username";
        String password = "password123!";

        MockHttpServletRequest request = createRequest(HttpMethod.GET, username, password);
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        ThrowingCallable action = () -> apiLoginFilter.attemptAuthentication(request, response);

        // then
        assertThatExceptionOfType(MethodNotAllowedException.class)
                .isThrownBy(action)
                .withMessage("Authentication method not supported: " + request.getMethod());
    }

    @Test
    void 잘못된_json_property() {
        // given
        String json = """
                {
                    "username": "username",
                    "pass": "password123!"
                }
                """;
        MockHttpServletRequest request = createRequest(json);
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        ThrowingCallable action = () -> apiLoginFilter.attemptAuthentication(request, response);

        // then
        assertThatExceptionOfType(InvalidJsonPropertyException.class)
                .isThrownBy(action)
                .satisfies(e -> assertThat(e.getPropertyName()).isEqualTo("pass"));
    }

    @Test
    void json_parsing_실패() {
        // given
        String json = """
                {
                    "username": "username",
                    "password": "password123!",
                }
                """;
        MockHttpServletRequest request = createRequest(json);
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        ThrowingCallable action = () -> apiLoginFilter.attemptAuthentication(request, response);

        // then
        assertThatExceptionOfType(InvalidJsonFormatException.class)
                .isThrownBy(action);
    }

    private MockHttpServletRequest createRequest(HttpMethod method, String username, String password) {
        return TestRequestBuilder.json()
                .method(method)
                .uri(REQUEST_URI)
                .body(new TestUser(username, password), objectMapper)
                .build();
    }

    private MockHttpServletRequest createRequest(String username, String password) {
        return TestRequestBuilder.json()
                .post(REQUEST_URI)
                .body(new TestUser(username, password), objectMapper)
                .build();
    }

    private MockHttpServletRequest createRequest(String json) {
        return TestRequestBuilder.json()
                .post(REQUEST_URI)
                .body(json)
                .build();
    }

    record TestUser(
            String username,
            String password
    ) {
    }
}