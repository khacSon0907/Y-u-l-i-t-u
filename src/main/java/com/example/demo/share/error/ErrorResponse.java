package com.example.demo.share.error;


import com.example.demo.share.BaseResponse;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuperBuilder
public final class ErrorResponse extends BaseResponse {

    public static ErrorResponse of(
            String type,        // VALIDATION | BUSINESS | SYSTEM
            int status,
            String code,
            String message,
            Object detail,
            String path,
            String traceId
    ) {
        return ErrorResponse.builder()
                .type(type)
                .title(httpTitle(status))
                .status(status)
                .code(code)
                .message(message)
                .detail(detail == null ? Map.of() : detail)
                .path(path)
                .traceId(traceId)
                .timestamp(Instant.now())
                .build();
    }

    public static String httpTitle(int status) {
        return switch (status) {
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 409 -> "Conflict";
            case 422 -> "Validation Error";
            case 429 -> "Too Many Requests";
            case 500 -> "Internal Server Error";
            default -> "Error";
        };
    }

    /* ===== helpers ===== */

    public static Map<String, List<String>> fieldError(String field, String message) {
        return Map.of(field, List.of(message));
    }

    public static Map<String, Object> detailOf(Object... kv) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            map.put(String.valueOf(kv[i]), kv[i + 1]);
        }
        return map;
    }
}
