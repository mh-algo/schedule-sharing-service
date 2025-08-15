package com.minhyung.schedule.security.login.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhyung.schedule.common.ApiResult;
import com.minhyung.schedule.common.exception.ErrorCode;
import com.minhyung.schedule.security.exception.AuthenticationErrorCode;
import com.minhyung.schedule.security.login.exception.InvalidJsonFormatException;
import com.minhyung.schedule.security.login.exception.InvalidJsonPropertyException;
import com.minhyung.schedule.security.exception.MethodNotAllowedException;
import com.minhyung.schedule.security.login.exception.LoginErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LoginAuthenticationFailureHandler implements AuthenticationFailureHandler {
    private ObjectMapper objectMapper;

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        if (exception instanceof BadCredentialsException) {   // 로그인 정보 불일치
            ErrorCode errorCode = LoginErrorCode.INVALID_CREDENTIALS;
            writeLoginFailureResponse(response, errorCode, errorCode.getMessage());
        } else if (exception instanceof DisabledException) {    // 계정 정지
            ErrorCode errorCode = LoginErrorCode.ACCOUNT_SUSPENDED;
            writeLoginFailureResponse(response, errorCode, errorCode.getMessage());
        } else if (exception instanceof MethodNotAllowedException) {   // post 요청 x
            ErrorCode errorCode = AuthenticationErrorCode.METHOD_NOT_ALLOWED;
            response.setHeader("Allow", HttpMethod.POST.name());
            writeLoginFailureResponse(response, errorCode, errorCode.getMessage());
        } else if (exception instanceof InvalidJsonPropertyException e) {    // 잘못된 json property
            ErrorCode errorCode = AuthenticationErrorCode.INVALID_JSON_PROPERTY;
            String message = String.format(errorCode.getMessage(), e.getPropertyName());
            writeLoginFailureResponse(response, errorCode, message);
        } else if (exception instanceof InvalidJsonFormatException) {   // 잘못된 요청 형식(json parsing 실패)
            ErrorCode errorCode = AuthenticationErrorCode.INVALID_JSON_FORMAT;
            writeLoginFailureResponse(response, errorCode, errorCode.getMessage());
        } else {
            ErrorCode errorCode = LoginErrorCode.LOGIN_FAILED;
            writeLoginFailureResponse(response, errorCode, errorCode.getMessage());
        }
    }

    private void writeLoginFailureResponse(HttpServletResponse response, ErrorCode errorCode, String message) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(errorCode.getStatus().value());
        String body = objectMapper.writeValueAsString(ApiResult.error(errorCode, message));
        response.getWriter().write(body);
    }
}
