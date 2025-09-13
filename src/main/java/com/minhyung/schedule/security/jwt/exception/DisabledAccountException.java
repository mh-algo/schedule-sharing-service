package com.minhyung.schedule.security.jwt.exception;

public class DisabledAccountException extends RuntimeException {
    public DisabledAccountException(String message) {
        super(message);
    }

    public DisabledAccountException(String message, Throwable cause) {
        super(message, cause);
    }
}
