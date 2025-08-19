package com.minhyung.schedule.security.configurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhyung.schedule.security.login.ApiLoginFilter;
import com.minhyung.schedule.security.login.handler.LoginAuthenticationFailureHandler;
import com.minhyung.schedule.security.login.handler.LoginAuthenticationSuccessHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class ApiLoginConfigurer extends AbstractHttpConfigurer<ApiLoginConfigurer, HttpSecurity> {
    private final ObjectMapper objectMapper;
    private final ApiLoginFilter authenticationFilter;
    private AuthenticationManager authenticationManager;
    private AuthenticationSuccessHandler successHandler;
    private AuthenticationFailureHandler failureHandler;
    private RequestMatcher loginProcessingUrlMatcher;

    public ApiLoginConfigurer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.authenticationFilter = new ApiLoginFilter(objectMapper);
    }

    @Override
    public void configure(HttpSecurity http) {
        authenticationFilter.setAuthenticationManager(authenticationManager);
        authenticationFilter.setAuthenticationSuccessHandler(successHandler);
        authenticationFilter.setAuthenticationFailureHandler(failureHandler);
        authenticationFilter.setRequiresAuthenticationRequestMatcher(loginProcessingUrlMatcher);
        http.setSharedObject(ApiLoginFilter.class, authenticationFilter);
        http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

        if (successHandler instanceof LoginAuthenticationSuccessHandler loginSuccessHandler) {
            loginSuccessHandler.setObjectMapper(objectMapper);
        }
        if (failureHandler instanceof LoginAuthenticationFailureHandler loginFailureHandler) {
            loginFailureHandler.setObjectMapper(objectMapper);
        }
    }

    public ApiLoginConfigurer authenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        return this;
    }

    public ApiLoginConfigurer successHandler(AuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
        return this;
    }

    public ApiLoginConfigurer failureHandler(AuthenticationFailureHandler failureHandler) {
        this.failureHandler = failureHandler;
        return this;
    }

    public ApiLoginConfigurer loginProcessingUrl(String loginProcessingUrl) {
        this.loginProcessingUrlMatcher = createLoginProcessingUrlMatcher(loginProcessingUrl);
        return this;
    }

    private RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
        return PathPatternRequestMatcher.withDefaults().matcher(loginProcessingUrl);
    }
}
