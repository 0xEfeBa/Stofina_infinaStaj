package com.stofina.app.orderservice.dto.portfolio;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Standard response DTO from Portfolio Service operations.
 * Provides consistent response structure for all Portfolio Service calls.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PortfolioResponse {

    private boolean success;

    private String message;

    private Object data;

    private LocalDateTime timestamp;

    private String errorCode;

    /**
     * Creates a successful response.
     * @param message success message
     * @return successful PortfolioResponse
     */
    public static PortfolioResponse success(String message) {
        return PortfolioResponse.builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a successful response with data.
     * @param message success message
     * @param data response data
     * @return successful PortfolioResponse with data
     */
    public static PortfolioResponse success(String message, Object data) {
        return PortfolioResponse.builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a failure response.
     * @param message error message
     * @return failed PortfolioResponse
     */
    public static PortfolioResponse failure(String message) {
        return PortfolioResponse.builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a failure response with error code.
     * @param message error message
     * @param errorCode specific error code
     * @return failed PortfolioResponse with error code
     */
    public static PortfolioResponse failure(String message, String errorCode) {
        return PortfolioResponse.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Checks if the response indicates success.
     * @return true if operation was successful
     */
    public boolean isSuccessful() {
        return success;
    }

    /**
     * Checks if the response indicates failure.
     * @return true if operation failed
     */
    public boolean isFailure() {
        return !success;
    }
}