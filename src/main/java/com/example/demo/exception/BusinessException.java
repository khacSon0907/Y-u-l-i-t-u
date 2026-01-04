package com.example.demo.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorDescriptor error;
    private final Object detail; // field error / extra info (optional)

    public BusinessException(ErrorDescriptor error) {
        super(error.defaultMessage());
        this.error = error;
        this.detail = null;
    }

    public BusinessException(ErrorDescriptor error, String customMessage) {
        super(customMessage);
        this.error = error;
        this.detail = null;
    }

    public BusinessException(ErrorDescriptor error, Object detail) {
        super(error.defaultMessage());
        this.error = error;
        this.detail = detail;
    }

    public BusinessException(ErrorDescriptor error, String customMessage, Object detail) {
        super(customMessage);
        this.error = error;
        this.detail = detail;
    }

    public String messageToClient() {
        return getMessage();
    }
}
