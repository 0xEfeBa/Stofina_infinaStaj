package com.stofina.app.orderservice.service;

public interface ScheduledOrderBookService {
    
    // CHECKPOINT 6.1 - Scheduled Order Book Service Contract
    
    void scheduledOrderBookMaintenance();
    
    void updateAllMarketPrices();
    
    void processActiveUserOrders();
    
    void refreshDisplayOrderBooks();
    
    void processUserOrdersForSymbol(String symbol);
    
    boolean isScheduledTaskEnabled();
    
    void enableScheduledTask();
    
    void disableScheduledTask();
}