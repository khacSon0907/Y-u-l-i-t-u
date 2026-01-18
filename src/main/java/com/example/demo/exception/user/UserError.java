package com.example.demo.exception.user;

import com.example.demo.exception.ErrorDescriptor;

public enum UserError implements ErrorDescriptor {

    EMAIL_EXISTS(
            "BUSINESS",
            409,
            "USER.EMAIL_EXISTS",
            "Email đã tồn tại"
    ),

    USERNAME_EXISTS(
            "BUSINESS",
            409,
            "USER.USERNAME_EXISTS",
            "Username đã tồn tại"
    ),

    USERNAME_INVALID(
            "BUSINESS",
            400,
            "USER.USERNAME_INVALID",
            "Username không hợp lệ"
    ),

    UNDER_AGE(
            "BUSINESS",
            400,
            "USER.UNDER_AGE",
            "Người dùng chưa đủ tuổi"
    ),

    // --- LOGIN ---
    INVALID_CREDENTIALS(
            "AUTH",
            401,
            "AUTH.INVALID_CREDENTIALS",
            "Mật khẩu không đúng"
    ),

    USER_NOT_FOUND(
            "AUTH",
            404,
            "AUTH.USER_NOT_FOUND",
            "Không tìm thấy người dùng"
    ),
    EMAIL_NOT_VERIFIED(
        "BUSINESS",
                409,
                "USER.EMAIL_NOT_VERIFIED",
                "Email đã đăng ký nhưng chưa xác nhận"
    );


    private final String type;
    private final int httpStatus;
    private final String code;
    private final String defaultMessage;

    UserError(String type, int httpStatus, String code, String defaultMessage) {
        this.type = type;
        this.httpStatus = httpStatus;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    @Override
    public String type() {
        return type;
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
}
