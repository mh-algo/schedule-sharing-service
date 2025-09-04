package com.minhyung.schedule.security.configurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhyung.schedule.security.auth.JwtAuthenticationFilter;
import com.minhyung.schedule.security.auth.handler.JwtAuthenticationFailureHandler;
import com.minhyung.schedule.security.jwt.service.JwtService;
import com.minhyung.schedule.security.login.ApiLoginFilter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

public class JwtAuthenticationConfigurer extends AbstractHttpConfigurer<JwtAuthenticationConfigurer, HttpSecurity> {
    private final ObjectMapper objectMapper;
    private final JwtAuthenticationFilter authenticationFilter;
    private AuthenticationManager authenticationManager;
    private AuthenticationSuccessHandler successHandler;
    private AuthenticationFailureHandler failureHandler;

    public JwtAuthenticationConfigurer(ObjectMapper objectMapper, JwtService jwtService) {
        this.objectMapper = objectMapper;
        this.authenticationFilter = new JwtAuthenticationFilter(jwtService);
    }

    @Override
    public void configure(HttpSecurity http) {
        authenticationFilter.setAuthenticationManager(authenticationManager);
        authenticationFilter.setAuthenticationSuccessHandler(successHandler);
        authenticationFilter.setAuthenticationFailureHandler(failureHandler);
        http.setSharedObject(JwtAuthenticationFilter.class, authenticationFilter);
        http.addFilterBefore(authenticationFilter, ApiLoginFilter.class);

        if (failureHandler instanceof JwtAuthenticationFailureHandler jwtFailureHandler) {
            jwtFailureHandler.setObjectMapper(objectMapper);
        }
    }

    public JwtAuthenticationConfigurer authenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        return this;
    }

    public JwtAuthenticationConfigurer successHandler(AuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
        return this;
    }

    public JwtAuthenticationConfigurer failureHandler(AuthenticationFailureHandler failureHandler) {
        this.failureHandler = failureHandler;
        return this;
    }
}
