package com.example.demo.exception;

import com.example.demo.share.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final int UNPROCESSABLE_ENTITY = 422;

    /* =====================================================
     * 1. BUSINESS EXCEPTION
     * ===================================================== */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest req
    ) {
        ErrorDescriptor error = ex.getError();

        log.warn("[BUSINESS_ERROR] code={}, message={}",
                error.code(), ex.messageToClient());

        return buildErrorResponse(
                error.type(),
                error.httpStatus(),
                error.code(),
                ex.messageToClient(),
                ex.getDetail(),
                req
        );
    }

    /* =====================================================
     * 2. VALIDATION – @Valid DTO
     * ===================================================== */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest req
    ) {
        Map<String, List<String>> fieldErrors = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(err ->
                fieldErrors
                        .computeIfAbsent(err.getField(), k -> new ArrayList<>())
                        .add(err.getDefaultMessage())
        );

        log.warn("[VALIDATION_ERROR] path={}, errors={}",
                req.getRequestURI(), fieldErrors);

        return buildErrorResponse(
                "VALIDATION",
                UNPROCESSABLE_ENTITY,
                CommonError.VALIDATION_ERROR.code(),
                CommonError.VALIDATION_ERROR.defaultMessage(),
                fieldErrors,
                req
        );
    }

    /* =====================================================
     * 3. VALIDATION – @RequestParam / @PathVariable
     * ===================================================== */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest req
    ) {
        Map<String, List<String>> fieldErrors = new LinkedHashMap<>();

        ex.getConstraintViolations().forEach(v -> {
            String field = v.getPropertyPath().toString();
            fieldErrors
                    .computeIfAbsent(field, k -> new ArrayList<>())
                    .add(v.getMessage());
        });

        log.warn("[CONSTRAINT_VIOLATION] path={}, errors={}",
                req.getRequestURI(), fieldErrors);

        return buildErrorResponse(
                "VALIDATION",
                UNPROCESSABLE_ENTITY,
                CommonError.VALIDATION_ERROR.code(),
                CommonError.VALIDATION_ERROR.defaultMessage(),
                fieldErrors,
                req
        );
    }

    /* =====================================================
     * 4. ILLEGAL ARGUMENT
     * ===================================================== */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest req
    ) {
        log.warn("[ILLEGAL_ARGUMENT] path={}, message={}",
                req.getRequestURI(), ex.getMessage());

        return buildErrorResponse(
                CommonError.INVALID_REQUEST.type(),
                CommonError.INVALID_REQUEST.httpStatus(),
                CommonError.INVALID_REQUEST.code(),
                ex.getMessage(),
                null,
                req
        );
    }

    /* =====================================================
     * 5. STATIC RESOURCE (favicon, etc.)
     * ===================================================== */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResourceFound() {
        // Không log – không phải lỗi hệ thống
        return ResponseEntity.notFound().build();
    }

    /* =====================================================
     * 6. FALLBACK – SYSTEM ERROR
     * ===================================================== */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception ex,
            HttpServletRequest req
    ) {
        log.error("[SYSTEM_ERROR] path={}", req.getRequestURI(), ex);

        return buildErrorResponse(
                CommonError.INTERNAL_SERVER_ERROR.type(),
                CommonError.INTERNAL_SERVER_ERROR.httpStatus(),
                CommonError.INTERNAL_SERVER_ERROR.code(),
                CommonError.INTERNAL_SERVER_ERROR.defaultMessage(),
                null,
                req
        );
    }

    /* =====================================================
     * HELPER
     * ===================================================== */
    private ResponseEntity<ErrorResponse> buildErrorResponse(
            String type,
            int status,
            String code,
            String message,
            Object detail,
            HttpServletRequest req
    ) {
        return ResponseEntity
                .status(status)
                .body(ErrorResponse.of(
                        type,
                        status,
                        code,
                        message,
                        detail,
                        req.getRequestURI(),
                        MDC.get("traceId")
                ));
    }
}
