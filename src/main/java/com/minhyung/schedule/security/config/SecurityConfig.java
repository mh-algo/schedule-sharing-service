package com.minhyung.schedule.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhyung.schedule.auth.service.UserService;
import com.minhyung.schedule.common.ApiPaths;
import com.minhyung.schedule.security.configurer.ApiLoginConfigurer;
import com.minhyung.schedule.security.jwt.service.JwtService;
import com.minhyung.schedule.security.login.ApiLoginFilter;
import com.minhyung.schedule.security.login.LoginAuthenticationProvider;
import com.minhyung.schedule.security.login.LoginUserDetailsService;
import com.minhyung.schedule.security.login.handler.LoginAuthenticationFailureHandler;
import com.minhyung.schedule.security.login.handler.LoginAuthenticationSuccessHandler;
import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final JwtService jwtService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AuthenticationManager authenticationManager = getApiLoginAuthenticationManager();

        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .with(new ApiLoginConfigurer(objectMapper), config -> config
                        .authenticationManager(authenticationManager)
                        .loginProcessingUrl(ApiPaths.AUTH + "/login")
                        .successHandler(new LoginAuthenticationSuccessHandler(jwtService))
                        .failureHandler(new LoginAuthenticationFailureHandler())
                )
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers(ApiPaths.AUTH + "/login").permitAll()
                        .anyRequest().permitAll());     // TODO: 나중에 인증 구현 후 수정

        return http.build();
    }

    private AuthenticationManager getApiLoginAuthenticationManager() {
        return new ProviderManager(getLoginAuthenticationProvider());
    }

    private AuthenticationProvider getLoginAuthenticationProvider() {
        LoginUserDetailsService userDetailsService = new LoginUserDetailsService(userService);
        return new LoginAuthenticationProvider(userDetailsService);
    }
}
