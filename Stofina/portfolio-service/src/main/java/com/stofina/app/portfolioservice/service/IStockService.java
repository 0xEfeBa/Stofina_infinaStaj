package com.stofina.app.portfolioservice.service;

import com.stofina.app.portfolioservice.dto.StockDto;
import com.stofina.app.portfolioservice.request.account.TransferStockRequest;
import com.stofina.app.portfolioservice.request.stock.BuyStockRequest;
import com.stofina.app.portfolioservice.request.stock.SellStockRequest;

import java.util.List;

/**
 * Service interface for managing stock trading operations,
 * including buy/sell workflows and order confirmations.
 */
public interface IStockService {

    /**
     * Places a buy order by creating a pending transaction and reserving funds.
     * No money is deducted from the account until the order is settled.
     *
     * @param request the buy stock request containing account, orderId, symbol, quantity, and price
     */
    void buyStock(BuyStockRequest request);

    /**
     * Confirms a buy order after it has been filled.
     * Deducts the reserved funds, updates stock holdings, and finalizes the transaction.
     *
     * @param orderId the ID of the order that was filled
     */
    void confirmBuy(Long orderId);

    /**
     * Cancels a buy order and releases the reserved funds.
     * A refund transaction is recorded for traceability.
     *
     * @param orderId the ID of the order that was canceled
     */
    void cancelBuy(Long orderId);

    /**
     * Confirms a buy order after it has been filled.
     * Deducts the reserved funds, updates stock holdings, and finalizes the transaction.
     *
     * @param orderId the ID of the order that was filled
     * @param fulfilledQuantity  the number of the stocks that were sold
     */
    void confirmBuyPartially(Long orderId, int fulfilledQuantity );


    /**
     * Places a sell order by creating a pending transaction and updating balances.
     * Proceeds are restricted under T+2 rules and will be released after settlement.
     *
     * @param request the sell stock request containing account, orderId, symbol, quantity, and price
     */
    void sellStock(SellStockRequest request);


    /**
     * Confirms a sell order after it has been filled.
     * Deducts the reserved funds, updates stock holdings, and finalizes the transaction.
     *
     * @param orderId the ID of the order that was filled
     */
    void confirmSell(Long orderId);


    /**
     * Confirms a sell order after it has been filled.
     * Deducts the reserved funds, updates stock holdings, and finalizes the transaction.
     *
     * @param orderId the ID of the order that was filled
     * @param fulfilledQuantity the ID of the order that was sold

     */
    void confirmSellPartially(Long orderId, int fulfilledQuantity );

    /**
     * Cancels a sell order and releases the reserved funds.
     * A refund transaction is recorded for traceability.
     *
     * @param orderId the ID of the order that was canceled
     */
    void cancelSell(Long orderId);


    /**
     * Retrieves all stocks owned by the given account.
     *
     * @param accountId the account ID
     * @return list of stocks belonging to the account
     */
    List<StockDto> getStocksByAccountId(Long accountId);

    /**
     * Retrieves a specific stock owned by the account and symbol.
     *
     * @param accountId the account ID
     * @param symbol the stock symbol (e.g., THYAO)
     * @return the stock DTO, or null if not found
     */
    StockDto getStockByAccountIdAndSymbol(Long accountId, String symbol);
    /**
     *   Transfers stock (by symbol and quantity) from one account to another.
     *   @param request the transfer details including source, destination, and stock quantity
     */
    void transferStock( TransferStockRequest request);
}



