package com.stofina.app.orderservice.service;

/**
 * Service interface for scheduled order book maintenance operations.
 * Handles periodic updates and maintenance of order books.
 */
public interface ScheduledOrderBookService {
    
    /**
     * Performs scheduled maintenance on all order books.
     * This includes cleaning up stale orders and updating order states.
     */
    void scheduledOrderBookMaintenance();
    
    /**
     * Updates market prices for all active symbols.
     * Fetches and updates current market prices from the price feed.
     */
    void updateAllMarketPrices();
    
    /**
     * Processes all active user orders against current market conditions.
     * Checks for matches and executes trades where appropriate.
     */
    void processActiveUserOrders();
    
    /**
     * Refreshes all display order books with current market data.
     * Updates the view of order books shown to users.
     */
    void refreshDisplayOrderBooks();
    
    /**
     * Processes all user orders for a specific symbol.
     *
     * @param symbol the stock symbol to process orders for
     */
    void processUserOrdersForSymbol(String symbol);
    
    /**
     * Checks if scheduled tasks are currently enabled.
     *
     * @return true if scheduled tasks are enabled, false otherwise
     */
    boolean isScheduledTaskEnabled();
    
    /**
     * Enables scheduled order book maintenance tasks.
     */
    void enableScheduledTask();
    
    /**
     * Disables scheduled order book maintenance tasks.
     */
    void disableScheduledTask();
}