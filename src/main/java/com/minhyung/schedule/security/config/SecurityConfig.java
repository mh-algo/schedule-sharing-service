package com.minhyung.schedule.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhyung.schedule.auth.service.UserService;
import com.minhyung.schedule.common.ApiPathsUtils;
import com.minhyung.schedule.security.auth.ApiAccessDeniedHandler;
import com.minhyung.schedule.security.auth.ApiAuthenticationEntryPoint;
import com.minhyung.schedule.security.auth.JwtAuthenticationProvider;
import com.minhyung.schedule.security.auth.handler.JwtAuthenticationFailureHandler;
import com.minhyung.schedule.security.auth.handler.JwtAuthenticationSuccessHandler;
import com.minhyung.schedule.security.configurer.ApiLoginConfigurer;
import com.minhyung.schedule.security.configurer.JwtAuthenticationConfigurer;
import com.minhyung.schedule.security.jwt.service.JwtService;
import com.minhyung.schedule.security.login.LoginAuthenticationProvider;
import com.minhyung.schedule.security.login.LoginUserDetailsService;
import com.minhyung.schedule.security.login.handler.LoginAuthenticationFailureHandler;
import com.minhyung.schedule.security.login.handler.LoginAuthenticationSuccessHandler;
import jakarta.servlet.DispatcherType;
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
        AuthenticationManager authenticationManager = getAuthenticationManager();

        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .with(new ApiLoginConfigurer(objectMapper), config -> config
                        .authenticationManager(authenticationManager)
                        .loginProcessingUrl(ApiPathsUtils.auth("login"))
                        .successHandler(new LoginAuthenticationSuccessHandler(jwtService))
                        .failureHandler(new LoginAuthenticationFailureHandler())
                )
                .with(new JwtAuthenticationConfigurer(objectMapper, jwtService), config -> config
                        .authenticationManager(authenticationManager)
                        .successHandler(new JwtAuthenticationSuccessHandler())
                        .failureHandler(new JwtAuthenticationFailureHandler())
                )
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(ApiPathsUtils.auth("login")).permitAll()
                        .requestMatchers(ApiPathsUtils.auth("signup")).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new ApiAuthenticationEntryPoint(objectMapper))
                        .accessDeniedHandler(new ApiAccessDeniedHandler(objectMapper)));
        return http.build();
    }

    private AuthenticationManager getAuthenticationManager() {
        return new ProviderManager(getJwtAuthenticationProvider(), getLoginAuthenticationProvider());
    }

    private AuthenticationProvider getJwtAuthenticationProvider() {
        return new JwtAuthenticationProvider(jwtService);
    }

    private AuthenticationProvider getLoginAuthenticationProvider() {
        LoginUserDetailsService userDetailsService = new LoginUserDetailsService(userService);
        return new LoginAuthenticationProvider(userDetailsService);
    }
}
