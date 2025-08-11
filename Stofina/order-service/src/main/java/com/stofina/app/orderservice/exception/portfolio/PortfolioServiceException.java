package com.stofina.app.orderservice.exception.portfolio;

/**
 * Base exception for all Portfolio Service related errors.
 * This is the parent class for all portfolio-specific exceptions.
 */
public class PortfolioServiceException extends RuntimeException {

    private final String errorCode;

    public PortfolioServiceException(String message) {
        super(message);
        this.errorCode = "PORTFOLIO_ERROR";
    }

    public PortfolioServiceException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public PortfolioServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "PORTFOLIO_ERROR";
    }

    public PortfolioServiceException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Checks if this exception is due to service unavailability.
     * @return true if this is a service availability issue
     */
    public boolean isServiceUnavailable() {
        return this instanceof PortfolioServiceUnavailableException;
    }

    /**
     * Checks if this exception is due to business rule violation.
     * @return true if this is a business rule violation
     */
    public boolean isBusinessRuleViolation() {
        return this instanceof InsufficientBalanceException || 
               this instanceof InsufficientStockException;
    }

    /**
     * Gets a user-friendly error message.
     * @return formatted error message for end users
     */
    public String getUserFriendlyMessage() {
        return String.format("Portfolio operation failed: %s (Code: %s)", getMessage(), errorCode);
    }
}