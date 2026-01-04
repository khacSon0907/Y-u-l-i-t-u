package com.example.demo.exception;

import org.springframework.http.HttpStatus;

public enum CommonError implements ErrorDescriptor {

    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "COMMON.INTERNAL_ERROR",
            "Internal server error",
            "SYSTEM"
    ),

    INVALID_REQUEST(
            HttpStatus.BAD_REQUEST.value(),
            "COMMON.INVALID_REQUEST",
            "Invalid request",
            "VALIDATION"
    ),

    VALIDATION_ERROR(
            422,
            "COMMON.VALIDATION_ERROR",
            "Validation failed",
            "VALIDATION"
    ),

    UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED.value(),
            "COMMON.UNAUTHORIZED",
            "Unauthorized",
            "SYSTEM"
    ),

    FORBIDDEN(
            HttpStatus.FORBIDDEN.value(),
            "COMMON.FORBIDDEN",
            "Access denied",
            "SYSTEM"
    ),

    NOT_FOUND(
            HttpStatus.NOT_FOUND.value(),
            "COMMON.NOT_FOUND",
            "Resource not found",
            "SYSTEM"
    );

    private final int httpStatus;
    private final String code;
    private final String defaultMessage;
    private final String type;

    CommonError(int httpStatus, String code, String defaultMessage, String type) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.type = type;
    }

    @Override
    public int httpStatus() {
        return httpStatus;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String defaultMessage() {
        return defaultMessage;
    }

    @Override
    public String type() {
        return type;
    }
}
