package com.minhyung.schedule.security.auth.handler;

import com.minhyung.schedule.security.auth.JwtAuthenticationToken;
import com.minhyung.schedule.security.jwt.JwtHeader;
import com.minhyung.schedule.security.jwt.JwtToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

public class JwtAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Object credentials = authentication.getCredentials();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            jwtAuthentication.eraseToken();
        }
        if (credentials instanceof JwtToken token) {
            addJwtTokenToHeader(response, token);
        } else {
            throw new InternalAuthenticationServiceException("Unexpected credentials type");
        }
    }

    private void addJwtTokenToHeader(HttpServletResponse response, JwtToken token) {
        response.setHeader(JwtHeader.ACCESS_TOKEN, token.getAuthHeader());
        response.setHeader(JwtHeader.REFRESH_TOKEN, token.getRefreshHeader());
    }
}
