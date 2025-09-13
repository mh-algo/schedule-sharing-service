package com.minhyung.schedule.common.exception;

import com.minhyung.schedule.common.ApiResult;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {
    // API 예외 처리
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResult<Void>> handleApiException(ApiException e) {
        log.warn("ApiException: {}", e.getMessage());
        return ResponseEntity.status(e.getStatus())
                .body(ApiResult.error(e));
    }

    // @Valid 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<Void> handleValidationException(MethodArgumentNotValidException e) {
        String globalErrorMessage = e.getBindingResult().getGlobalErrors().stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));

        String fieldErrorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        String errorMessage = mergeErrorMessage(globalErrorMessage, fieldErrorMessage);
        log.warn("MethodArgumentNotValidException: {}", errorMessage);
        return ApiResult.error(ValidationErrorCode.VALIDATION_EXCEPTION, errorMessage);
    }

    private static String mergeErrorMessage(String globalErrorMessage, String fieldErrorMessage) {
        if (globalErrorMessage.isEmpty()) {
            return fieldErrorMessage;
        } else if (fieldErrorMessage.isEmpty()) {
            return globalErrorMessage;
        }
        return globalErrorMessage + ", " + fieldErrorMessage;
    }

    // @Validated 예외 처리
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(this::formatViolationMessage)
                .collect(Collectors.joining(", "));
        log.warn("ConstraintViolationException: {}", errorMessage);
        return ApiResult.error(ValidationErrorCode.VALIDATION_EXCEPTION, errorMessage);
    }

    private String formatViolationMessage(ConstraintViolation<?> violation) {
        String fullPath = violation.getPropertyPath().toString();
        String field = fullPath.substring(fullPath.lastIndexOf(".") + 1); // 필드명만 추출
        return field + ": " + violation.getMessage();
    }

    // 필수 파라미터 누락
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<Void> handleMissingParams(MissingServletRequestParameterException e) {
        String errorMessage = String.format(ValidationErrorCode.MISSING_REQUIRED_PARAM.getMessage(), e.getParameterName());
        log.warn("MissingServletRequestParameterException: {}", errorMessage);
        return ApiResult.error(ValidationErrorCode.MISSING_REQUIRED_PARAM, errorMessage);
    }

    // 지원하지 않는 경로
    @ExceptionHandler(value = NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResult<Void> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("NoHandlerFoundException: {}", e.getMessage());
        return ApiResult.error(ClientErrorCode.NOT_FOUND);
    }

    // 지원하지 않는 메서드
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiResult<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("HttpRequestMethodNotSupportedException: {}", e.getMessage());
        return ApiResult.error(ClientErrorCode.METHOD_NOT_ALLOWED);
    }

    // 무결성 제약조건 위반
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResult<Void> handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException e) {
        log.error("SQLIntegrityConstraintViolationException: {}", e.getMessage(), e);
        return ApiResult.error(ServerErrorCode.SERVER_ERROR);
    }

    // DB 에러
    @ExceptionHandler(SQLException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult<Void> handleSQLException(SQLException e) {
        log.error("SQLException: {}", e.getMessage(), e);
        return ApiResult.error(ServerErrorCode.SERVER_ERROR);
    }
}
