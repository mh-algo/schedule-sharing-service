package com.minhyung.schedule.security.login.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidJsonPropertyException extends AuthenticationException {
    private String propertyName;

    public InvalidJsonPropertyException(String message) {
        super(message);
    }

    public InvalidJsonPropertyException(String message, Throwable cause, String propertyName) {
        super(message, cause);
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }
}
