package com.stofina.app.portfolioservice.service.impl;

import com.stofina.app.portfolioservice.dto.TransactionDto;
import com.stofina.app.portfolioservice.enums.TransactionType;
import com.stofina.app.portfolioservice.exception.TransactionNotFoundException;
import com.stofina.app.portfolioservice.mapper.TransactionMapper;
import com.stofina.app.portfolioservice.model.Transaction;
import com.stofina.app.portfolioservice.repository.TransactionRepository;
import com.stofina.app.portfolioservice.service.ITransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements ITransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Override
    public List<TransactionDto> getTransactionHistory(Long accountId) {
        List<Transaction> transactions = transactionRepository.findByAccountId(accountId);
        return transactionMapper.toTransactionDtoList(transactions);
    }

    @Override
    public TransactionDto getTransactionById(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with id: " + transactionId));
        return transactionMapper.toTransactionDto(transaction);
    }

    @Override
    public TransactionSummary getSummaryByAccount(Long accountId) {
        List<Transaction> transactions = transactionRepository.findByAccountId(accountId);

        int totalBuyQty = 0, totalSellQty = 0;
        BigDecimal totalBuyAmount = BigDecimal.ZERO;
        BigDecimal totalSellAmount = BigDecimal.ZERO;

        for (Transaction tx : transactions) {
            if (tx.getTransactionType() == TransactionType.BUY) {
                totalBuyQty += tx.getQuantity();
                totalBuyAmount = totalBuyAmount.add(tx.getAmount());
            } else if (tx.getTransactionType() == TransactionType.SELL) {
                totalSellQty += tx.getQuantity();
                totalSellAmount = totalSellAmount.add(tx.getAmount());
            }
        }

        ITransactionService.TransactionSummary summary = new ITransactionService.TransactionSummary();
        summary.totalBuyQuantity = totalBuyQty;
        summary.totalSellQuantity = totalSellQty;
        summary.totalBuyAmount = totalBuyAmount.doubleValue();
        summary.totalSellAmount = totalSellAmount.doubleValue();

        log.info("Transaction summary calculated for account {}: BUY {} ({}₺), SELL {} ({}₺)",
                accountId, totalBuyQty, totalBuyAmount, totalSellQty, totalSellAmount);

        return summary;
    }
}
