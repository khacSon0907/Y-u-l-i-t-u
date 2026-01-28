package com.example.demo.exception.auth;

import com.example.demo.exception.ErrorDescriptor;

public enum AuthError implements ErrorDescriptor {

    TOO_MANY_OTP_ATTEMPTS(
            "AUTH",
            429,
            "AUTH.TOO_MANY_OTP_ATTEMPTS",
            "Nhập OTP sai quá nhiều. Vui lòng thử lại sau."
    ),

    INVALID_REFRESH_TOKEN(
            "AUTH", 400,
            "AUTH.INVALID_REFRESH_TOKEN",
            "Refresh token không hợp lệ hoặc đã hết hạn"
    ),

    REFRESH_TOKEN_NOT_FOUND(
            "AUTH", 401,
            "AUTH.REFRESH_TOKEN_NOT_FOUND",
            "Refresh token không tồn tại hoặc đã bị thu hồi"
    ),

    INVALID_VERIFY_TOKEN(
            "AUTH", 400,
            "AUTH.INVALID_VERIFY_TOKEN",
            "Token xác minh không hợp lệ hoặc đã hết hạn"
    ),

    // 🆕 OTP
    INVALID_OTP(
            "AUTH", 400,
            "AUTH.INVALID_OTP",
            "OTP không hợp lệ hoặc đã hết hạn"
    ),

    // 🆕 RESET PASSWORD
    INVALID_RESET_TOKEN(
            "AUTH", 400,
            "AUTH.INVALID_RESET_TOKEN",
            "Reset password token không hợp lệ hoặc đã hết hạn"
    ),

    TOO_MANY_LOGIN_ATTEMPTS(
            "AUTH", 429,
            "AUTH.TOO_MANY_LOGIN_ATTEMPTS",
            "Quá nhiều lần đăng nhập thất bại. Vui lòng thử lại sau."
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
