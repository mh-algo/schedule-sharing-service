package com.minhyung.schedule.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhyung.schedule.auth.service.UserService;
import com.minhyung.schedule.common.ApiPaths;
import com.minhyung.schedule.security.login.ApiLoginFilter;
import com.minhyung.schedule.security.login.LoginAuthenticationProvider;
import com.minhyung.schedule.security.login.LoginUserDetailsService;
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
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {
    private final ObjectMapper objectMapper;
    private final UserService userService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(createApiLoginFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .anyRequest().permitAll());     // TODO: 나중에 인증 구현 후 수정

        return http.build();
    }

    private Filter createApiLoginFilter() {
        ApiLoginFilter filter = new ApiLoginFilter(objectMapper);
        filter.setAuthenticationManager(getApiLoginAuthenticationManager());
        filter.setRequiresAuthenticationRequestMatcher(PathPatternRequestMatcher.withDefaults().matcher(ApiPaths.AUTH + "/login"));
        return filter;
    }

    private AuthenticationManager getApiLoginAuthenticationManager() {
        return new ProviderManager(getLoginAuthenticationProvider());
    }

    private AuthenticationProvider getLoginAuthenticationProvider() {
        LoginUserDetailsService userDetailsService = new LoginUserDetailsService(userService);
        return new LoginAuthenticationProvider(userDetailsService);
    }
}
