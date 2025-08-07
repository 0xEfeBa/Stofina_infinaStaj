package com.stofina.app.market_data_service.service;

import java.time.LocalDateTime;

public interface IMarketHoursService {

  
    boolean isMarketOpen();
  
    boolean isWithinMarketHours(LocalDateTime dateTime);
    
  
    LocalDateTime getNextMarketOpen();
    
   
    LocalDateTime getNextMarketClose();
    
    /**
     * Piyasanın ne zaman açılacağını döner
     * @return Piyasanın açılma zamanı
     */
    LocalDateTime getNextOpenTime();
    
    /**
     * Piyasanın ne zaman kapanacağını döner
     * @return Piyasanın kapanma zamanı
     */
    LocalDateTime getNextCloseTime();
    
    /**
     * Piyasa durumunu string olarak döner
     * @return Piyasa durumu
     */
    String getMarketStatus();
}
