package com.minhyung.schedule.security.login.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhyung.schedule.common.ApiResult;
import com.minhyung.schedule.security.jwt.JwtHeader;
import com.minhyung.schedule.security.jwt.service.JwtService;
import com.minhyung.schedule.security.jwt.JwtToken;
import com.minhyung.schedule.security.principal.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LoginAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public LoginAuthenticationSuccessHandler(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            JwtToken jwtToken = jwtService.issueJwtToken(userPrincipal);
            writeLoginSuccessResponse(response, jwtToken);
        } else {
            throw new InternalAuthenticationServiceException("Unexpected principal type");
        }
    }

    private void writeLoginSuccessResponse(HttpServletResponse response, JwtToken jwtToken) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader(JwtHeader.ACCESS_TOKEN, jwtToken.getAuthHeader());
        response.setHeader(JwtHeader.REFRESH_TOKEN, jwtToken.getRefreshHeader());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String body = objectMapper.writeValueAsString(ApiResult.success("로그인 성공"));
        response.getWriter().write(body);
    }
}
