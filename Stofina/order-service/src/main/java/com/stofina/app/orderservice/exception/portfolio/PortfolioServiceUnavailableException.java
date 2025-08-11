package com.stofina.app.orderservice.exception.portfolio;

/**
 * Exception thrown when Portfolio Service is temporarily unavailable.
 * This is used for circuit breaker fallbacks and retry scenarios.
 */
public class PortfolioServiceUnavailableException extends PortfolioServiceException {

    public PortfolioServiceUnavailableException(String message) {
        super(message, "PORTFOLIO_SERVICE_UNAVAILABLE");
    }

    public PortfolioServiceUnavailableException(String message, Throwable cause) {
        super(message, "PORTFOLIO_SERVICE_UNAVAILABLE", cause);
    }

    @Override
    public String getUserFriendlyMessage() {
        return "Portfolio service is temporarily unavailable. Please try again later.";
    }

    /**
     * Checks if this is a timeout-related unavailability.
     * @return true if the root cause is a timeout
     */
    public boolean isTimeoutRelated() {
        Throwable cause = getCause();
        return cause != null && (
            cause.getClass().getSimpleName().contains("Timeout") ||
            cause.getMessage() != null && cause.getMessage().toLowerCase().contains("timeout")
        );
    }

    /**
     * Checks if this is a connection-related unavailability.
     * @return true if the root cause is a connection issue
     */
    public boolean isConnectionRelated() {
        Throwable cause = getCause();
        return cause != null && (
            cause.getClass().getSimpleName().contains("Connection") ||
            cause.getMessage() != null && cause.getMessage().toLowerCase().contains("connection")
        );
    }
}