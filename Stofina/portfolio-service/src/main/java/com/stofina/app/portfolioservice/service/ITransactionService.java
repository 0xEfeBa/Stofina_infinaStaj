package com.stofina.app.portfolioservice.service;

import com.stofina.app.portfolioservice.dto.TransactionDto;

import java.util.List;

/**
 * Service interface for transaction history, detail, and summary operations.
 */
public interface ITransactionService {

    /**
     * Retrieves paged transaction history for a given account.
     *
     * @param accountId the account ID
     * @return a paginated list of transaction DTOs
     */
    List<TransactionDto>  getTransactionHistory(Long accountId);

    /**
     * Retrieves transaction detail by ID.
     *
     * @param transactionId the transaction ID
     * @return the transaction DTO
     */
    TransactionDto getTransactionById(Long transactionId);

    /**
     * Returns a basic summary of BUY/SELL transactions for a given account.
     *
     * @param accountId the account ID
     * @return transaction summary object (e.g., totalBuy, totalSell, etc.)
     */
    TransactionSummary getSummaryByAccount(Long accountId);

    /**
     * Summary projection object (can be returned from REST).
     */
    class TransactionSummary {
        public int totalBuyQuantity;
        public int totalSellQuantity;
        public double totalBuyAmount;
        public double totalSellAmount;
    }
}
