package com.minhyung.schedule.security.exception;

import org.springframework.security.core.AuthenticationException;

public class MethodNotAllowedException extends AuthenticationException {
    private final String supportedMethod;

    public MethodNotAllowedException(String message, String supportedMethod) {
        super(message);
        this.supportedMethod = supportedMethod;
    }

    public String getSupportedMethod() {
        return supportedMethod;
    }
}
