package com.stofina.app.orderservice.service;

import com.stofina.app.orderservice.entity.Order;
import com.stofina.app.orderservice.model.DisplayOrder;
import com.stofina.app.orderservice.model.SimpleOrderBookSnapshot;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Service interface for managing display-only order books.
 * Handles the presentation layer of order books shown to users.
 */
public interface DisplayOrderBookService {
    
    /**
     * Initializes a new display order book for a symbol.
     *
     * @param symbol the stock symbol
     * @param currentPrice the current market price
     */
    void initializeDisplayOrderBook(String symbol, BigDecimal currentPrice);
    
    /**
     * Updates display prices for a symbol's order book.
     *
     * @param symbol the stock symbol
     * @param newPrice the new market price
     */
    void updateDisplayPrices(String symbol, BigDecimal newPrice);
    
    /**
     * Gets a snapshot of the current display order book.
     *
     * @param symbol the stock symbol
     * @return current state of the display order book
     */
    SimpleOrderBookSnapshot getDisplaySnapshot(String symbol);
    
    /**
     * Adds a user order to the display order book.
     *
     * @param userOrder the order to display
     */
    void addUserOrderToDisplay(Order userOrder);
    
    /**
     * Removes an order from the display order book.
     *
     * @param symbol the stock symbol
     * @param orderId the ID of the order to remove
     */
    void removeFromDisplay(String symbol, Long orderId);
    
    /**
     * Gets all symbols that have active display order books.
     *
     * @return set of active symbols
     */
    Set<String> getActiveDisplaySymbols();
    
    /**
     * Gets the total number of orders in a display order book.
     *
     * @param symbol the stock symbol
     * @return number of orders
     */
    int getDisplayOrderCount(String symbol);
    
    /**
     * Gets the best bid price from the display order book.
     *
     * @param symbol the stock symbol
     * @return best bid price
     */
    BigDecimal getDisplayBestBid(String symbol);
    
    /**
     * Gets the best ask price from the display order book.
     *
     * @param symbol the stock symbol
     * @return best ask price
     */
    BigDecimal getDisplayBestAsk(String symbol);
    
    /**
     * Clears all orders from a display order book.
     *
     * @param symbol the stock symbol
     */
    void clearDisplayOrderBook(String symbol);
    
    /**
     * Maintains the display order book depth by removing excess orders.
     *
     * @param symbol the stock symbol
     * @param currentPrice the current market price
     */
    void maintainDisplayDepth(String symbol, BigDecimal currentPrice);
    
    /**
     * Gets all user orders for a specific symbol.
     *
     * @param symbol the stock symbol
     * @return list of user orders
     */
    List<DisplayOrder> getUserOrdersForSymbol(String symbol);
}