package com.minhyung.schedule.security.login.exception;

import org.springframework.security.core.AuthenticationException;

public class MethodNotAllowedException extends AuthenticationException {
    public MethodNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }

    public MethodNotAllowedException(String message) {
        super(message);
    }
}
