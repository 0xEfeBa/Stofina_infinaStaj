package com.stofina.app.marketdataservice.service;

import com.stofina.app.marketdataservice.common.ServiceResult;
import com.stofina.app.marketdataservice.constant.Messages;
import com.stofina.app.marketdataservice.entity.Stock;
import com.stofina.app.marketdataservice.repository.StockRepository;
import com.stofina.app.marketdataservice.service.impl.IStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements IStockService {

    @Autowired
    private StockRepository stockRepository;

    @Override
    public ServiceResult<List<Stock>> getAllStocks() {
        List <Stock> stocks = stockRepository.findAllByOrderBySymbolAsc();
        if(stocks.isEmpty()){
            log.warn("Veritabanında hiç stok kaydı bulunamadı.");
            return ServiceResult.failure(Messages.NO_STOCKS_AVAILABLE);
        }

        log.info("Toplam {} adet stok kaydı bulundu.", stocks.size());
        return ServiceResult.success(stocks,Messages.STOCKS_RETRIEVED);

    };

    @Override
    public ServiceResult<Stock> getStockBySymbol(String symbol) {
         Optional<Stock> stockOptional = stockRepository.findBySymbol(symbol.toUpperCase());

         if(stockOptional.isPresent()){
             Stock stock = stockOptional.get();
             log.info("Stok bilgisi bulundu: {}", stock.getSymbol());
             return ServiceResult.success(stock, Messages.STOCKS_RETRIEVED);
         }
            log.warn("Stok bilgisi bulunamadı: {}", symbol);
            return ServiceResult.failure(Messages.STOCK_NOT_FOUND);


    }

    @Override
    public ServiceResult<Stock> updateStockPrice(String symbol, BigDecimal newPrice) {
         Optional<Stock> stockOptional = stockRepository.findBySymbol(symbol.toUpperCase());

         if(stockOptional.isPresent()){
             Stock stock = stockOptional.get();
             stock.setCurrentPrice(newPrice);
             stockRepository.save(stock);
             log.info("Stok fiyatı güncellendi: {} - Yeni fiyat: {}", stock.getSymbol(), newPrice);
             return ServiceResult.success(stock, Messages.STOCK_UPDATED);
         }
            log.warn("Stok fiyatı güncellenemedi, stok bulunamadı: {}", symbol);
            return ServiceResult.failure(Messages.STOCK_NOT_FOUND);

    }

    @Override
    public ServiceResult<List<Stock>> resetAllPricesToDefault() {
        List <Stock> stocks = stockRepository.findAllByOrderBySymbolAsc();

        if(stocks.isEmpty()){
            log.warn("Veritabanında stok kaydı bulunamadı.");
            return ServiceResult.failure(Messages.NO_STOCKS_AVAILABLE);
        }

        stocks.forEach(stock -> {
            stock.setCurrentPrice(stock.getDefaultPrice());
            stockRepository.save(stock);
        });

        log.info("Tüm stok fiyatları sıfırlandı.");
        return ServiceResult.success(stocks, Messages.ALL_PRICES_RESET);

    }

    @Override
    public ServiceResult<Boolean> isValidSymbol(String symbol) {
        boolean exists = stockRepository.findBySymbol(symbol.toUpperCase()).isPresent();
        if (exists) {
            log.info("Symbol '{}' is valid.", symbol);
            return ServiceResult.success(true, Messages.VALID_SYMBOL); // NOT failure!
        } else {
            log.warn("Symbol '{}' is not valid.", symbol);
            return ServiceResult.success(false, Messages.INVALID_SYMBOL); // NOT failure!
        }
    }

    @Override
    public void save(Stock stock) {
        stockRepository.save(stock);
    }
}
