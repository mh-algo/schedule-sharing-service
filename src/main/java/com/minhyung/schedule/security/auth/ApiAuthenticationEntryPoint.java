package com.minhyung.schedule.security.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhyung.schedule.common.ApiResult;
import com.minhyung.schedule.common.exception.ErrorCode;
import com.minhyung.schedule.security.exception.AuthenticationErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    public ApiAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException {
        ErrorCode errorCode = codeOf(e);

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(errorCode.getStatus().value());
        String body = objectMapper.writeValueAsString(ApiResult.error(errorCode));
        response.getWriter().write(body);
    }

    private ErrorCode codeOf(AuthenticationException e) {
        if (e instanceof InsufficientAuthenticationException) {     // 인증 x
            return AuthenticationErrorCode.NOT_AUTHENTICATED;
        } else {    // 서버 에러
            return AuthenticationErrorCode.AUTHENTICATION_FAILED;
        }
    }
}
