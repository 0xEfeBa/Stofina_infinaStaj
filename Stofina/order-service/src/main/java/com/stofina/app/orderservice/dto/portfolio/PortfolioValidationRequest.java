package com.stofina.app.orderservice.dto.portfolio;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Request DTO for Portfolio Service validation operations.
 * Used to validate if sufficient balance or stock exists before order creation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioValidationRequest {

    @NotNull(message = "Account ID cannot be null")
    @Positive(message = "Account ID must be positive")
    private Long accountId;

    @NotBlank(message = "Symbol cannot be blank")
    @Pattern(regexp = "^[A-Z]{4,6}$", message = "Symbol must be 4-6 uppercase letters")
    private String symbol;

    @NotNull(message = "Validation type cannot be null")
    private ValidationType validationType;

    @DecimalMin(value = "0.01", message = "Required amount must be greater than 0.01")
    @Digits(integer = 15, fraction = 4, message = "Amount format is invalid")
    private BigDecimal requiredAmount;

    @Positive(message = "Required quantity must be positive")
    private Integer requiredQuantity;

    /**
     * Creates a balance validation request.
     * @param accountId the account ID to validate
     * @param requiredAmount the amount needed
     * @return PortfolioValidationRequest for balance validation
     */
    public static PortfolioValidationRequest forBalanceValidation(Long accountId, BigDecimal requiredAmount) {
        return PortfolioValidationRequest.builder()
                .accountId(accountId)
                .validationType(ValidationType.SUFFICIENT_BALANCE)
                .requiredAmount(requiredAmount)
                .build();
    }

    /**
     * Creates a stock validation request.
     * @param accountId the account ID to validate
     * @param symbol the stock symbol
     * @param requiredQuantity the quantity needed
     * @return PortfolioValidationRequest for stock validation
     */
    public static PortfolioValidationRequest forStockValidation(Long accountId, String symbol, Integer requiredQuantity) {
        return PortfolioValidationRequest.builder()
                .accountId(accountId)
                .symbol(symbol.toUpperCase())
                .validationType(ValidationType.SUFFICIENT_STOCK)
                .requiredQuantity(requiredQuantity)
                .build();
    }

    /**
     * Enum defining different types of portfolio validations.
     */
    public enum ValidationType {
        SUFFICIENT_BALANCE("Sufficient Balance Check"),
        SUFFICIENT_STOCK("Sufficient Stock Check"),
        ACCOUNT_STATUS("Account Status Check");

        private final String description;

        ValidationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}