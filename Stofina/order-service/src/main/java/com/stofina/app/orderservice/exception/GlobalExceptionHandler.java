package com.stofina.app.orderservice.exception;

import com.stofina.app.orderservice.dto.response.ValidationErrorResponse;
import com.stofina.app.orderservice.exception.portfolio.PortfolioServiceException;
import com.stofina.app.orderservice.exception.portfolio.InsufficientBalanceException;
import com.stofina.app.orderservice.exception.portfolio.InsufficientStockException;
import com.stofina.app.orderservice.exception.portfolio.PortfolioCompensationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<String> handleOrderNotFound(OrderNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidOrderException.class)
    public ResponseEntity<Object> handleInvalidOrder(InvalidOrderException ex) {
        return ResponseEntity.badRequest().body(
                String.format("Error Code: %s | Field: %s | Message: %s",
                        ex.getErrorCode(), ex.getField(), ex.getMessage())
        );
    }

    // PORTFOLIO SERVICE EXCEPTION HANDLERS

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<Map<String, Object>> handlePortfolioInsufficientBalance(InsufficientBalanceException ex) {
        log.error("🏦 PORTFOLIO EXCEPTION: Insufficient Balance → AccountId: {}, Required: {}, Available: {}", 
                ex.getAccountId(), ex.getRequiredAmount(), ex.getAvailableAmount());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "INSUFFICIENT_BALANCE");
        errorResponse.put("message", ex.getUserFriendlyMessage());
        errorResponse.put("accountId", ex.getAccountId());
        errorResponse.put("requiredAmount", ex.getRequiredAmount());
        errorResponse.put("availableAmount", ex.getAvailableAmount());
        errorResponse.put("shortageAmount", ex.getShortageAmount());
        errorResponse.put("timestamp", java.time.LocalDateTime.now());
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, Object>> handlePortfolioInsufficientStock(InsufficientStockException ex) {
        log.error("🏦 PORTFOLIO EXCEPTION: Insufficient Stock → AccountId: {}, Symbol: {}, Required: {}, Available: {}", 
                ex.getAccountId(), ex.getSymbol(), ex.getRequiredQuantity(), ex.getAvailableQuantity());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "INSUFFICIENT_STOCK");
        errorResponse.put("message", ex.getUserFriendlyMessage());
        errorResponse.put("accountId", ex.getAccountId());
        errorResponse.put("symbol", ex.getSymbol());
        errorResponse.put("requiredQuantity", ex.getRequiredQuantity());
        errorResponse.put("availableQuantity", ex.getAvailableQuantity());
        errorResponse.put("shortageQuantity", ex.getShortageQuantity());
        errorResponse.put("shortagePercentage", ex.getShortagePercentage());
        errorResponse.put("timestamp", java.time.LocalDateTime.now());
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(PortfolioCompensationException.class)
    public ResponseEntity<Map<String, Object>> handlePortfolioCompensation(PortfolioCompensationException ex) {
        log.error("🏦 PORTFOLIO EXCEPTION: Compensation Failed → OrderId: {}, TradeId: {}, Type: {}, Critical: {}", 
                ex.getOrderId(), ex.getTradeId(), ex.getCompensationType(), ex.isCritical());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "PORTFOLIO_COMPENSATION_FAILED");
        errorResponse.put("message", ex.getUserFriendlyMessage());
        errorResponse.put("orderId", ex.getOrderId());
        errorResponse.put("tradeId", ex.getTradeId());
        errorResponse.put("compensationType", ex.getCompensationType());
        errorResponse.put("critical", ex.isCritical());
        errorResponse.put("technicalDescription", ex.getTechnicalDescription());
        errorResponse.put("timestamp", java.time.LocalDateTime.now());
        
        // Critical compensation failures should return 500 as they require immediate attention
        HttpStatus status = ex.isCritical() ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(PortfolioServiceException.class)
    public ResponseEntity<Map<String, Object>> handlePortfolioService(PortfolioServiceException ex) {
        log.error("🏦 PORTFOLIO EXCEPTION: Portfolio Service Error → ErrorCode: {}, Message: {}", 
                ex.getErrorCode(), ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getErrorCode());
        errorResponse.put("message", ex.getUserFriendlyMessage());
        errorResponse.put("service", "PORTFOLIO_SERVICE");
        errorResponse.put("timestamp", java.time.LocalDateTime.now());
        
        // Map error codes to appropriate HTTP status codes
        HttpStatus status = switch (ex.getErrorCode()) {
            case "NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "INVALID_REQUEST" -> HttpStatus.BAD_REQUEST;
            case "CONFLICT" -> HttpStatus.CONFLICT;
            case "SERVICE_UNAVAILABLE", "TIMEOUT" -> HttpStatus.SERVICE_UNAVAILABLE;
            case "UNAUTHORIZED" -> HttpStatus.UNAUTHORIZED;
            case "FORBIDDEN" -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ValidationErrorResponse>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<ValidationErrorResponse> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new ValidationErrorResponse(
                        fieldError.getField(),
                        fieldError.getDefaultMessage(),
                        fieldError.getRejectedValue()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Unexpected error occurred: " + ex.getMessage());
    }
}