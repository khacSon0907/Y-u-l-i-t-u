package com.example.demo.config;

import com.example.demo.exception.CommonError;
import com.example.demo.share.error.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper; // ✅ inject từ Spring

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        response.setStatus(CommonError.UNAUTHORIZED.httpStatus());
        response.setContentType("application/json");

        ErrorResponse payload = ErrorResponse.of(
                CommonError.UNAUTHORIZED.type(),
                CommonError.UNAUTHORIZED.httpStatus(),
                CommonError.UNAUTHORIZED.code(),
                CommonError.UNAUTHORIZED.defaultMessage(),
                null,
                request.getRequestURI(),
                null
        );

        objectMapper.writeValue(response.getOutputStream(), payload);
    }
}
