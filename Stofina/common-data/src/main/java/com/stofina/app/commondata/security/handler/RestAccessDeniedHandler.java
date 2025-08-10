package com.stofina.app.commondata.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stofina.app.commondata.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Log4j2
@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        log.error("Forbidden error: {}", accessDeniedException.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("You don't have permission to access this resource.")
                .exceptionType(accessDeniedException.getClass().getSimpleName())
                .statusCode(HttpStatus.FORBIDDEN.value())
                .errorTime(LocalDateTime.now())
                .build();

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
