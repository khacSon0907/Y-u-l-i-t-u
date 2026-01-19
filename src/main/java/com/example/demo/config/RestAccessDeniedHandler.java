package com.example.demo.config;

import com.example.demo.exception.CommonError;
import com.example.demo.share.error.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(CommonError.FORBIDDEN.httpStatus());
        response.setContentType("application/json");

        var payload = ErrorResponse.of(
                CommonError.FORBIDDEN.type(),
                CommonError.FORBIDDEN.httpStatus(),
                CommonError.FORBIDDEN.code(),
                CommonError.FORBIDDEN.defaultMessage(),
                null,
                request.getRequestURI(),
                null
        );

        objectMapper.writeValue(response.getOutputStream(), payload);
    }
}

