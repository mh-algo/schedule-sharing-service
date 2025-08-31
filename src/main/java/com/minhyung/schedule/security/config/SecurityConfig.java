package com.minhyung.schedule.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhyung.schedule.auth.service.UserService;
import com.minhyung.schedule.common.ApiPathsUtils;
import com.minhyung.schedule.security.auth.JwtAuthenticationFilter;
import com.minhyung.schedule.security.auth.JwtAuthenticationProvider;
import com.minhyung.schedule.security.auth.handler.JwtAuthenticationSuccessHandler;
import com.minhyung.schedule.security.configurer.ApiLoginConfigurer;
import com.minhyung.schedule.security.jwt.service.JwtService;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final JwtService jwtService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AuthenticationManager authenticationManager = getAuthenticationManager();

        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(getJwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .with(new ApiLoginConfigurer(objectMapper), config -> config
                        .authenticationManager(authenticationManager)
                        .loginProcessingUrl(ApiPathsUtils.auth("login"))
                        .successHandler(new LoginAuthenticationSuccessHandler(jwtService))
                        .failureHandler(new LoginAuthenticationFailureHandler())
                )
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers(ApiPathsUtils.auth("login")).permitAll()
                        .anyRequest().authenticated());

        return http.build();
    }

    private AuthenticationManager getAuthenticationManager() {
        return new ProviderManager(getJwtAuthenticationProvider(), getLoginAuthenticationProvider());
    }

    private AuthenticationProvider getLoginAuthenticationProvider() {
        LoginUserDetailsService userDetailsService = new LoginUserDetailsService(userService);
        return new LoginAuthenticationProvider(userDetailsService);
    }

    private AuthenticationProvider getJwtAuthenticationProvider() {
        return new JwtAuthenticationProvider(jwtService);
    }

    private Filter getJwtAuthenticationFilter() {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        filter.setAuthenticationManager(getAuthenticationManager());
        filter.setAuthenticationSuccessHandler(new JwtAuthenticationSuccessHandler());
        filter.setAuthenticationFailureHandler(null);
        return filter;
    }
}
