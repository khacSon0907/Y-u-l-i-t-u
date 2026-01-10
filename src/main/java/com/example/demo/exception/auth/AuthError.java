package com.example.demo.exception.auth;

import com.example.demo.exception.ErrorDescriptor;

public enum AuthError implements ErrorDescriptor {

    INVALID_REFRESH_TOKEN(
            "AUTH",
            400,
            "AUTH.INVALID_REFRESH_TOKEN",
            "Refresh token không hợp lệ hoặc đã hết hạn"
    ),

    REFRESH_TOKEN_NOT_FOUND(
            "AUTH",
            401,
            "AUTH.REFRESH_TOKEN_NOT_FOUND",
            "Refresh token không tồn tại hoặc đã bị thu hồi"
    );

    private final String type;
    private final int httpStatus;
    private final String code;
    private final String defaultMessage;

    AuthError(String type, int httpStatus, String code, String defaultMessage) {
        this.type = type;
        this.httpStatus = httpStatus;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    @Override public String type() { return type; }
    @Override public int httpStatus() { return httpStatus; }
    @Override public String code() { return code; }
    @Override public String defaultMessage() { return defaultMessage; }
}
