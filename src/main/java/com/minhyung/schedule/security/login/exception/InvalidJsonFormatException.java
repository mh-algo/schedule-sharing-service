package com.minhyung.schedule.security.login.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidJsonFormatException extends AuthenticationException {
    public InvalidJsonFormatException(String message) {
        super(message);
    }

    public InvalidJsonFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
