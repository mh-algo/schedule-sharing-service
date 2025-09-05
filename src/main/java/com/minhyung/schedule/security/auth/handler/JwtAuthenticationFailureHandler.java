package com.minhyung.schedule.security.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhyung.schedule.common.ApiResult;
import com.minhyung.schedule.common.exception.ErrorCode;
import com.minhyung.schedule.security.auth.exception.InvalidJwtException;
import com.minhyung.schedule.security.auth.exception.TokenExpiredException;
import com.minhyung.schedule.security.exception.AuthenticationErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class JwtAuthenticationFailureHandler implements AuthenticationFailureHandler {
    private ObjectMapper objectMapper;

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        ErrorCode errorCode = toCode(exception);
        writeAuthenticationFailureResponse(response, errorCode);
    }

    private ErrorCode toCode(AuthenticationException exception) throws IOException {
        if (exception instanceof DisabledException ||        // 계정 정지
                exception instanceof TokenExpiredException ||       // 토큰 만료
                exception instanceof BadCredentialsException ||     // 사용자 정보 x
                exception instanceof InvalidJwtException) {         // 유효하지 않은 토큰
            return AuthenticationErrorCode.INVALID_AUTHENTICATION;
        }  else {    // 서버 에러
            return AuthenticationErrorCode.AUTHENTICATION_FAILED;
        }
    }

    private void writeAuthenticationFailureResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(errorCode.getStatus().value());
        String body = objectMapper.writeValueAsString(ApiResult.error(errorCode));
        response.getWriter().write(body);
    }
}
