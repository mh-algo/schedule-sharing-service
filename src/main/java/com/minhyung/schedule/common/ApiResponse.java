package com.minhyung.schedule.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.minhyung.schedule.common.exception.ApiException;
import com.minhyung.schedule.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiResponse<T> {
    private final int status;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String code;
    private final String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T data;

    private ApiResponse(HttpStatus status, String code, String message, T data) {
        this.status = status.value();
        this.code = code;
        this.message = message;
        this.data = data;
    }

    private ApiResponse(HttpStatus status, String code, String message) {
        this(status, code, message, null);
    }

    private ApiResponse(HttpStatus status, String message, T data) {
        this(status, null, message, data);
    }

    private ApiResponse(HttpStatus status, String message) {
        this(status, null, message, null);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(HttpStatus.OK,"success");
    }

    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(HttpStatus.OK, message);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(HttpStatus.OK, "success", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(HttpStatus.OK, message, data);
    }

    public static ApiResponse<Void> created(String message) {
        return new ApiResponse<>(HttpStatus.CREATED, message);
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(HttpStatus.CREATED, message, data);
    }

    public static ApiResponse<Void> error(ApiException e) {
        return new ApiResponse<>(e.getStatus(), e.getCode(), e.getMessage());
    }

    public static ApiResponse<Void> error(ErrorCode e) {
        return new ApiResponse<>(e.getStatus(), e.getCode(), e.getMessage());
    }

    public static ApiResponse<Void> error(ErrorCode e, String message) {
        return new ApiResponse<>(e.getStatus(), e.getCode(), message);
    }
}
