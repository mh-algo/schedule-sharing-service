package com.minhyung.schedule.security.auth;

import com.minhyung.schedule.auth.exception.UserNotFoundException;
import com.minhyung.schedule.security.auth.exception.AccessTokenExpiredException;
import com.minhyung.schedule.security.auth.exception.InvalidJwtException;
import com.minhyung.schedule.security.auth.exception.TokenExpiredException;
import com.minhyung.schedule.security.exception.MethodNotAllowedException;
import com.minhyung.schedule.security.jwt.JwtHeader;
import com.minhyung.schedule.security.jwt.JwtToken;
import com.minhyung.schedule.security.jwt.exception.DisabledAccountException;
import com.minhyung.schedule.security.jwt.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
    private AuthenticationManager authenticationManager;
    private AuthenticationSuccessHandler successHandler;
    private AuthenticationFailureHandler failureHandler;
    private final JwtService jwtService;
    private RequestMatcher logoutPathRequestMatcher =
            PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/api/*/auths/logout");

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public void setAuthenticationSuccessHandler(AuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    public void setAuthenticationFailureHandler(AuthenticationFailureHandler failureHandler) {
        this.failureHandler = failureHandler;
    }

    public void setLogoutPathRequestMatcher(RequestMatcher logoutPathRequestMatcher) {
        this.logoutPathRequestMatcher = logoutPathRequestMatcher;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        JwtToken token = JwtToken.ofBearer(
                request.getHeader(JwtHeader.ACCESS_TOKEN),
                request.getHeader(JwtHeader.REFRESH_TOKEN)
        );

        try {
            if (logoutPathRequestMatcher.matches(request)) {        // 로그아웃 경로일 경우 accessToken 인증 무시
                if (!request.getMethod().equals("POST")) {
                    throw new MethodNotAllowedException("Authentication method not supported: " + request.getMethod(), "POST");
                }
            } else {
                // Access Token 인증
                if (token.isEmpty()) {  // 토큰이 없을 경우
                    chain.doFilter(request, response);
                    return;
                }
                if (!token.hasBearerPrefix()) {     // "Bearer "로 시작하지 않는 경우
                    throw new InvalidJwtException("Invalid JWT token");
                }

                Authentication auth = attemptAuthentication(token);
                if (auth != null) {
                    successfulAuthentication(auth);
                    chain.doFilter(request, response);
                    return;
                }

                // Refresh Token 검증 후 재발급
                Authentication reAuth = reissueToken(token);
                if (reAuth != null) {
                    successfulAuthentication(request, response, reAuth);
                }
            }
            chain.doFilter(request, response);
        } catch (AuthenticationException e) {
            this.failureHandler.onAuthenticationFailure(request, response, e);
        }
    }

    private Authentication reissueToken(JwtToken token) {
        try {
            JwtToken reissued = jwtService.reissueJwtToken(token.getRefreshToken());    // token 재발급
            return attemptAuthentication(reissued);
        } catch (DisabledAccountException e) {      // 계정이 정지된 경우
            throw new DisabledException("Disabled");
        } catch (UserNotFoundException e) {     // 사용자 정보 조회 실패
            throw new BadCredentialsException("Bad credentials");
        } catch (ExpiredJwtException e) {       // 만료된 refresh 토큰
            throw new TokenExpiredException("Expired JWT token");
        } catch (JwtException | IllegalArgumentException e) {   // 유효하지 않은 토큰
            throw new InvalidJwtException(e.getMessage(), e);
        }
    }

    private Authentication attemptAuthentication(JwtToken token) {
        try {
            JwtAuthenticationToken authRequest = JwtAuthenticationToken.unauthenticated(token);
            return this.authenticationManager.authenticate(authRequest);
        } catch (AccessTokenExpiredException e) {
            return null;    // Access 만료는 바깥 흐름이 Refresh로 넘어가게 하기 위해 null로 신호
        }
    }

    private void successfulAuthentication(Authentication authentication) {
        SecurityContext context = this.securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        this.securityContextHolderStrategy.setContext(context);
    }

    private void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        successfulAuthentication(authentication);
        this.successHandler.onAuthenticationSuccess(request, response, authentication);
    }
}
