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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LoginAuthenticationFailureHandler implements AuthenticationFailureHandler {
    private final ObjectMapper objectMapper;

    public LoginAuthenticationFailureHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        ResponseDto responseDto = toResponseDto(exception);

        if (exception instanceof MethodNotAllowedException e) {
            response.setHeader("Allow", e.getSupportedMethod());
        }
        writeLoginFailureResponse(response, responseDto);
    }

    private ResponseDto toResponseDto(AuthenticationException exception) {
        ErrorCode errorCode;
        String message = null;

        if (exception instanceof BadCredentialsException) {   // 로그인 정보 불일치
            errorCode = LoginErrorCode.INVALID_CREDENTIALS;
        } else if (exception instanceof DisabledException) {    // 계정 정지
            errorCode = LoginErrorCode.ACCOUNT_SUSPENDED;
        } else if (exception instanceof MethodNotAllowedException) {   // 허용되지 않은 메서드 요청
            errorCode = AuthenticationErrorCode.METHOD_NOT_ALLOWED;
        } else if (exception instanceof InvalidJsonPropertyException e) {    // 잘못된 json property
            errorCode = AuthenticationErrorCode.INVALID_JSON_PROPERTY;
            message = String.format(errorCode.getMessage(), e.getPropertyName());
        } else if (exception instanceof InvalidJsonFormatException) {   // 잘못된 요청 형식(json parsing 실패)
            errorCode = AuthenticationErrorCode.INVALID_JSON_FORMAT;
        } else {
            errorCode = LoginErrorCode.LOGIN_FAILED;      // 서버 문제로 로그인 실패
        }

        return message == null
                ? new ResponseDto(errorCode)
                : new ResponseDto(errorCode, message);
    }

    private void writeLoginFailureResponse(HttpServletResponse response, ResponseDto responseDto) throws IOException {
        ErrorCode errorCode = responseDto.errorCode();
        String message = responseDto.message();

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(errorCode.getStatus().value());
        String body = objectMapper.writeValueAsString(ApiResult.error(errorCode, message));
        response.getWriter().write(body);
    }

    private record ResponseDto(
            ErrorCode errorCode,
            String message
    ) {
        private ResponseDto(ErrorCode errorCode) {
            this(errorCode, errorCode.getMessage());
        }
    }
}
