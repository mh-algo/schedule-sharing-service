package com.minhyung.schedule.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.minhyung.schedule.common.exception.ApiException;
import com.minhyung.schedule.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiResult<T> {
    private final int status;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String code;
    private final String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T data;

    private ApiResult(HttpStatus status, String code, String message, T data) {
        this.status = status.value();
        this.code = code;
        this.message = message;
        this.data = data;
    }

    private ApiResult(HttpStatus status, String code, String message) {
        this(status, code, message, null);
    }

    private ApiResult(HttpStatus status, String message, T data) {
        this(status, null, message, data);
    }

    private ApiResult(HttpStatus status, String message) {
        this(status, null, message, null);
    }

    public static ApiResult<Void> success() {
        return new ApiResult<>(HttpStatus.OK,"success");
    }

    public static ApiResult<Void> success(String message) {
        return new ApiResult<>(HttpStatus.OK, message);
    }

    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(HttpStatus.OK, "success", data);
    }

    public static <T> ApiResult<T> success(String message, T data) {
        return new ApiResult<>(HttpStatus.OK, message, data);
    }

    public static ApiResult<Void> created(String message) {
        return new ApiResult<>(HttpStatus.CREATED, message);
    }

    public static <T> ApiResult<T> created(String message, T data) {
        return new ApiResult<>(HttpStatus.CREATED, message, data);
    }

    public static ApiResult<Void> error(ApiException e) {
        return new ApiResult<>(e.getStatus(), e.getCode(), e.getMessage());
    }

    public static ApiResult<Void> error(ErrorCode e) {
        return new ApiResult<>(e.getStatus(), e.getCode(), e.getMessage());
    }

    public static ApiResult<Void> error(ErrorCode e, String message) {
        return new ApiResult<>(e.getStatus(), e.getCode(), message);
    }
}
