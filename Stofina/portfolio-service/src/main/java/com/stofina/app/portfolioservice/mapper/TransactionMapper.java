package com.stofina.app.portfolioservice.mapper;


import com.stofina.app.portfolioservice.dto.TransactionDto;
import com.stofina.app.portfolioservice.model.Transaction;
import com.stofina.app.portfolioservice.request.stock.BuyStockRequest;
import com.stofina.app.portfolioservice.request.stock.SellStockRequest;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    TransactionDto toTransactionDto(Transaction transaction);

    List<TransactionDto> toTransactionDtoList(List<Transaction> transaction);

    @Mapping(target = "transactionType", constant = "BUY")
    Transaction fromBuyRequest(BuyStockRequest request);

    @Mapping(target = "transactionType", constant = "SELL")
    Transaction fromSellRequest(SellStockRequest request);
}
