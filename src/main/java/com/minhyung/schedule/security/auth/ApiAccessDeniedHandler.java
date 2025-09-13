package com.minhyung.schedule.security.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhyung.schedule.common.ApiResult;
import com.minhyung.schedule.common.exception.ErrorCode;
import com.minhyung.schedule.security.exception.AuthenticationErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ApiAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    public ApiAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException {
        if (e instanceof AuthorizationDeniedException) {    // 권한 x
            writeAccessFailureResponse(response, AuthenticationErrorCode.INVALID_ACCESS);
        } else {    // 서버 에러
            writeAccessFailureResponse(response, AuthenticationErrorCode.AUTHORIZATION_FAILED);
        }
    }

    private void writeAccessFailureResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(errorCode.getStatus().value());
        String body = objectMapper.writeValueAsString(ApiResult.error(errorCode, errorCode.getMessage()));
        response.getWriter().write(body);
    }
}
