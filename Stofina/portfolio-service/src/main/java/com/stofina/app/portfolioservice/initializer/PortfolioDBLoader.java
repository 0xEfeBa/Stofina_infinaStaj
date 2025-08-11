package com.stofina.app.portfolioservice.initializer;

import com.stofina.app.portfolioservice.dto.AccountDto;
import com.stofina.app.portfolioservice.dto.StockDto;
import com.stofina.app.portfolioservice.repository.AccountRepository;
import com.stofina.app.portfolioservice.request.account.CreateAccountRequest;
import com.stofina.app.portfolioservice.request.stock.BuyStockRequest;
import com.stofina.app.portfolioservice.service.IAccountService;
import com.stofina.app.portfolioservice.service.IStockService;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PortfolioDBLoader {

    private final IAccountService accountService;
    private final IStockService stockService;
    private final AccountRepository accountRepository;

    @Bean
    ApplicationRunner seedRunner() {
        return args -> {
            log.warn(">>> SEED START");

            List<Long> individualIds = ids(0, 4);
            List<Long> corporateIds  = ids(5, 9);

            BigDecimal individualInit = new BigDecimal("250000.00");
            BigDecimal corporateInit  = new BigDecimal("2000000.00");

            List<SeedStock> priceList = List.of(
                    s("AKBNK","67.15"), s("CCOLA","49.92"), s("DOAS","183.10"),
                    s("MGROS","531.00"), s("FROTO","92.95"), s("TCELL","92.55"),
                    s("THYAO","290.25"), s("YEOTK","42.08"), s("BRSAN","354.00"),
                    s("TUPRS","164.50")
            );

            Map<String,Integer> qtyIndividual = Map.of(
                    "AKBNK",100, "TCELL",50, "THYAO",20, "TUPRS",15
            );
            Map<String,Integer> qtyCorporate = Map.of(
                    "AKBNK",2000, "MGROS",500, "FROTO",800, "THYAO",400, "TUPRS",600
            );

            List<AccountDto> individualAccs = ensureAccounts(individualIds, individualInit);
            List<AccountDto> corporateAccs  = ensureAccounts(corporateIds,  corporateInit);

            AtomicLong orderSeq = new AtomicLong(System.currentTimeMillis());

            for (AccountDto acc : individualAccs) {
                seedPortfolioForAccount(acc.getId(), priceList, qtyIndividual, orderSeq);
            }
            for (AccountDto acc : corporateAccs) {
                seedPortfolioForAccount(acc.getId(), priceList, qtyCorporate, orderSeq);
            }

            log.warn("<<< SEED DONE");
        };
    }

    private List<AccountDto> ensureAccounts(List<Long> customerIds, BigDecimal initialBalance) {
        List<AccountDto> out = new ArrayList<>();
        for (Long cid : customerIds) {
            var existing = accountRepository.findByCustomerId(cid);
            if (existing != null && !existing.isEmpty()) {
                out.add(accountService.getAccountById(existing.get(0).getId()));
            } else {
                AccountDto created = accountService.createAccount(
                        CreateAccountRequest.builder()
                                .customerId(cid)
                                .initialBalance(initialBalance)
                                .openingDate(LocalDate.now())
                                .build()
                );
                out.add(created);
            }
        }
        return out;
    }

    private void seedPortfolioForAccount(Long accountId,
                                         List<SeedStock> prices,
                                         Map<String,Integer> qtyMap,
                                         AtomicLong orderSeq) {

        for (SeedStock p : prices) {
            Integer qty = ThreadLocalRandom.current().nextInt(1, 11);            if (qty == null || qty <= 0) continue;

            StockDto existing = stockService.getStockByAccountIdAndSymbol(accountId, p.getSymbol());
            if (existing != null) {
                continue;
            }

            long orderId = orderSeq.incrementAndGet();

            stockService.buyStock(BuyStockRequest.builder()
                    .accountId(accountId)
                    .symbol(p.getSymbol())
                    .orderId(orderId)
                    .quantity(qty)
                    .price(p.getPrice())
                    .description("seed buy")
                    .build());

            stockService.confirmBuy(orderId);
        }
    }

    private static List<Long> ids(long start, long endInclusive) {
        List<Long> r = new ArrayList<>();
        for (long i = start; i <= endInclusive; i++) r.add(i);
        return r;
    }

    private static SeedStock s(String symbol, String price) {
        return new SeedStock(symbol, new BigDecimal(price));
    }

    @Value
    private static class SeedStock {
        String symbol;
        BigDecimal price;
    }
}
