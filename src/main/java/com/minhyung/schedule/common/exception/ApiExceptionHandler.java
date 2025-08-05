package com.minhyung.schedule.common.exception;

import com.minhyung.schedule.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {
    // API 예외 처리
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException e) {
        log.warn("ApiException: {}", e.getMessage());
        return ResponseEntity.status(e.getStatus())
                .body(ApiResponse.error(e));
    }

    // 무결성 제약조건 위반
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException e) {
        log.error("SQLIntegrityConstraintViolationException: {}", e.getMessage(), e);
        return ApiResponse.error(ServerErrorCode.DB_CONSTRAINT_VIOLATION);
    }

    // DB 에러
    @ExceptionHandler(SQLException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleSQLException(SQLException e) {
        log.error("SQLException: {}", e.getMessage(), e);
        return ApiResponse.error(ServerErrorCode.DB_ERROR);
    }
}
