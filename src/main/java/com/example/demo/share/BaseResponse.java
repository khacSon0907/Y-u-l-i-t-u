package com.example.demo.share;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.Instant;

@Getter
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "type", "title", "status", "code", "message",
        "data", "detail", "path", "traceId", "timestamp"
})
public abstract class BaseResponse implements Serializable {

    protected final String type;       // SUCCESS | VALIDATION | BUSINESS | SYSTEM
    protected final String title;      // Success | Validation Error | Conflict
    protected final Integer status;    // HTTP status
    protected final String code;       // SUCCESS | USER.EMAIL_EXISTS
    protected final String message;    // Message cho FE

    protected final Object data;       // success payload
    protected final Object detail;     // error detail

    protected final String path;       // Request URI
    protected final String traceId;    // Correlation ID
    protected final Instant timestamp; // UTC
}
