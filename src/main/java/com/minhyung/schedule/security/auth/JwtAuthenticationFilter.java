package com.minhyung.schedule.security.auth;

import com.minhyung.schedule.security.auth.exception.AccessTokenExpiredException;
import com.minhyung.schedule.security.jwt.JwtHeader;
import com.minhyung.schedule.security.jwt.JwtToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
    private AuthenticationManager authenticationManager;
    private AuthenticationSuccessHandler successHandler;
    private AuthenticationFailureHandler failureHandler;

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public void setAuthenticationSuccessHandler(AuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    public void setAuthenticationFailureHandler(AuthenticationFailureHandler failureHandler) {
        this.failureHandler = failureHandler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        try {
            Authentication authResult = attemptAuthentication(request);
            if (authResult != null) {
                successfulAuthentication(authResult);
            }
            chain.doFilter(request, response);
        } catch (AccessTokenExpiredException e) {
            // TODO: Refresh Token 검증 후 재발급
        } catch (AuthenticationException e) {
            this.failureHandler.onAuthenticationFailure(request, response, e);
        }
    }

    private Authentication attemptAuthentication(HttpServletRequest request) {
        String authHeader = request.getHeader(JwtHeader.ACCESS_TOKEN);
        String refreshHeader = request.getHeader(JwtHeader.REFRESH_TOKEN);
        JwtToken token = JwtToken.ofBearer(authHeader, refreshHeader);
        if (token.isEmpty()) {  // 토큰이 없을 경우
            return null;
        }
        JwtAuthenticationToken authRequest = JwtAuthenticationToken.unauthenticated(token);
        return this.authenticationManager.authenticate(authRequest);
    }

    private void successfulAuthentication(Authentication authentication) {
        SecurityContext context = this.securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        this.securityContextHolderStrategy.setContext(context);
    }
}
