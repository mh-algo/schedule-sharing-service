package com.minhyung.schedule.security.auth;

import com.minhyung.schedule.security.auth.exception.AccessTokenExpiredException;
import com.minhyung.schedule.security.auth.exception.InvalidJwtException;
import com.minhyung.schedule.security.jwt.JwtToken;
import com.minhyung.schedule.security.jwt.service.JwtService;
import com.minhyung.schedule.security.principal.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public class JwtAuthenticationProvider implements AuthenticationProvider {
    private static final GrantedAuthority DEFAULT_ROLE = new SimpleGrantedAuthority("ROLE_USER");
    private static final List<GrantedAuthority> DEFAULT_AUTHORITIES = List.of(DEFAULT_ROLE);
    private final JwtService jwtService;

    public JwtAuthenticationProvider(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        JwtToken token = (JwtToken) authentication.getCredentials();
        if (!token.hasBearerPrefix()) {     // "Bearer "로 시작하지 않는 경우
            throw new InvalidJwtException("Invalid JWT token");
        }
        try {
            String accessToken = token.getAccessToken();
            Claims claims = jwtService.parseClaims(accessToken);
            UserPrincipal principal = toPrincipal(claims);
            return JwtAuthenticationToken.authenticated(principal, token, DEFAULT_AUTHORITIES);
        } catch (ExpiredJwtException e) {   // accessToken 만료
            throw new AccessTokenExpiredException("Expired Access token");
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidJwtException(e.getMessage(), e);
        }
    }

    public UserPrincipal toPrincipal(Claims claims) {
        Long id = Long.valueOf(claims.getSubject());
        Boolean verified = claims.get("verified", Boolean.class);
        return new UserPrincipal(id, verified);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
