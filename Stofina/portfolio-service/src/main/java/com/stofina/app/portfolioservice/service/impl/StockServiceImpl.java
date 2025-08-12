package com.stofina.app.portfolioservice.service.impl;

import com.stofina.app.portfolioservice.exception.NotEnoughBalanceException;
import com.stofina.app.portfolioservice.dto.StockDto;
import com.stofina.app.portfolioservice.enums.*;
import com.stofina.app.portfolioservice.exception.*;
import com.stofina.app.portfolioservice.mapper.StockMapper;
import com.stofina.app.portfolioservice.mapper.TransactionMapper;
import com.stofina.app.portfolioservice.model.*;
import com.stofina.app.portfolioservice.repository.*;
import com.stofina.app.portfolioservice.request.account.TransferStockRequest;
import com.stofina.app.portfolioservice.request.stock.BuyStockRequest;
import com.stofina.app.portfolioservice.request.stock.SellStockRequest;
import com.stofina.app.portfolioservice.service.IStockService;
import com.stofina.app.portfolioservice.util.AccountBalanceCalculator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockServiceImpl implements IStockService {

    private final AccountRepository accountRepository;
    private final StockRepository stockRepository;
    private final TransactionRepository transactionRepository;
    private final BalanceReservationRepository reservationRepository;
    private final WithdrawalRestrictionRepository restrictionRepository;
    private final TransactionMapper transactionMapper;
    private final StockMapper stockMapper;
    private final StockReservationRepository stockReservationRepository;
    private final AccountBalanceCalculator withdrawableBalanceComponent;
    @Override
    @Transactional
    public void buyStock(BuyStockRequest request) {
        Account account = findAccount(request.getAccountId());

        BigDecimal totalCost = request.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        if (totalCost.compareTo(account.getAvailableBalance()) > 0) {
            throw new NotEnoughBalanceException("Not enough balance");
        }
        // 1. Create reservation
        BalanceReservation reservation = BalanceReservation.builder()
                .accountId(account.getId())
                .orderId(request.getOrderId())
                .reservedAmount(totalCost)
                .reservationType(ReservationType.BUY_ORDER)
                .status(ReservationStatus.ACTIVE)
                .usedAmount(BigDecimal.ZERO)
                .reservationDate(LocalDateTime.now())
                .description("Buy order for " + request.getSymbol())
                .build();
        reservationRepository.save(reservation);

        // 2. Create transaction (PENDING)
        Transaction transaction = transactionMapper.fromBuyRequest(request);
        transaction.setTradeDate(LocalDateTime.now());
        transaction.setSettlementStatus(SettlementStatus.PENDING);
        transaction.setTransactionStatus(TransactionStatus.PENDING);
        transaction.setAmount(totalCost);
        transaction.setBalanceBeforeTransaction(account.getTotalBalance());
        transaction.setBalanceAfterTransaction(account.getTotalBalance()); // No change yet
        transactionRepository.save(transaction);

        // 3. Update account balance
        account.setAvailableBalance(account.getAvailableBalance().subtract(totalCost));
        account.setReservedBalance(account.getReservedBalance().add(totalCost));
        withdrawableBalanceComponent.recalculateWithdrawableBalance(account);
        accountRepository.save(account);

        log.info("Buy order placed for account {}: {} x {} reserved (orderId={})",
                account.getId(), request.getQuantity(), request.getSymbol(), request.getOrderId());
    }

    @Override
    @Transactional
    public void confirmBuy(Long orderId) {
        Transaction transaction = getTransactionBuOrderId(orderId);

        if (transaction.getTransactionStatus() != TransactionStatus.PENDING &&
                transaction.getTransactionStatus() != TransactionStatus.PARTIALLY_SETTLED) {
            throw new OrderAlreadySettledException("Buy order {} already confirmed or cancelled with Order Id:"+ orderId);
        }

        Account account = findAccount(transaction.getAccountId());
        BigDecimal totalCost = transaction.getAmount();
        if (totalCost.compareTo(account.getAvailableBalance()) > 0) {
            throw new NotEnoughBalanceException("Not enough balance");
        }

        BigDecimal amount = transaction.getPrice().multiply(BigDecimal.valueOf(transaction.getQuantity()-transaction.getFulfilledQuantity()));

        // Update transaction
        transaction.setTransactionStatus(TransactionStatus.SETTLED);
        transaction.setSettlementStatus(SettlementStatus.SETTLED);
        transaction.setSettlementDate(LocalDateTime.now());
        transaction.setBalanceAfterTransaction(account.getTotalBalance().subtract(amount));
        transactionRepository.save(transaction);

        // Update stock
        Stock stock = stockRepository.findByAccountIdAndSymbol(account.getId(), transaction.getSymbol())
                .orElse(Stock.builder()
                        .accountId(account.getId())
                        .symbol(transaction.getSymbol())
                        .quantity(0)
                        .averageCost(BigDecimal.ZERO)
                        .build());

        int newQuantity = transaction.getQuantity()-transaction.getFulfilledQuantity();
        int buyQuantity = transaction.getQuantity() - transaction.getFulfilledQuantity();
        int totalQuantity = stock.getQuantity() + buyQuantity;

        BigDecimal totalExistingCost = stock.getAverageCost()
                .multiply(BigDecimal.valueOf(stock.getQuantity()));

        BigDecimal totalNewCost = totalExistingCost.add(amount);

        BigDecimal newAverageCost = totalNewCost
                .divide(BigDecimal.valueOf(totalQuantity), 4, RoundingMode.HALF_UP);

        stock.setQuantity(newQuantity+stock.getQuantity());
        stock.setAverageCost(newAverageCost);
        stockRepository.save(stock);

        // Update account balances
        account.setTotalBalance(account.getTotalBalance().subtract(amount));
        account.setReservedBalance(account.getReservedBalance().subtract(amount));
        withdrawableBalanceComponent.recalculateWithdrawableBalance(account);
        accountRepository.save(account);

        // Release reservation
        BalanceReservation reservation = reservationRepository.findByAccountIdAndOrderId(account.getId(), orderId)
                .orElseThrow(() -> new BalanceReservationNotFound("Reservation not found for order ID: " + orderId));
        reservation.setStatus(ReservationStatus.RELEASED);
        reservation.setUsedAmount(amount);
        reservationRepository.save(reservation);

        log.info("Buy order confirmed for account {}. Stock {} updated. Amount {} settled.",
                account.getId(), stock.getSymbol(), amount);
    }

    @Override
    @Transactional
    public void cancelBuy(Long orderId) {
        Transaction transaction = getTransactionBuOrderId(orderId);

        Account account = findAccount(transaction.getAccountId());
        BalanceReservation reservation = reservationRepository.findByAccountIdAndOrderId(account.getId(), orderId)
                .orElseThrow(() -> new BalanceReservationNotFound("Reservation not found for order ID: " + orderId));

        int originalQuantity = transaction.getQuantity();
        int fulfilledQuantity = transaction.getFulfilledQuantity();
        int remainingQuantity = originalQuantity - fulfilledQuantity;

        if (remainingQuantity <= 0) {
            log.info("No remaining quantity to cancel for order {}", orderId);
            return;
        }
        BigDecimal refundAmount = transaction.getPrice().multiply(BigDecimal.valueOf(remainingQuantity));
        BigDecimal balanceBefore=transaction.getPrice().multiply(BigDecimal.valueOf(fulfilledQuantity)).add(account.getTotalBalance()).add(refundAmount);
        // Set transaction as FAILED (or PARTIALLY_FAILED)
        transaction.setTransactionStatus(TransactionStatus.FAILED);
        transaction.setSettlementStatus(SettlementStatus.FAILED);
        transactionRepository.save(transaction);

        // Create refund transaction
        Transaction refund = Transaction.builder()
                .accountId(account.getId())
                .symbol("TRY")
                .transactionType(TransactionType.DEPOSIT)
                .transactionStatus(TransactionStatus.REFUNDED)
                .settlementStatus(SettlementStatus.SETTLED)
                .amount(refundAmount)
                .price(BigDecimal.ZERO)
                .quantity(0)
                .tradeDate(LocalDateTime.now())
                .settlementDate(LocalDateTime.now())
                .balanceBeforeTransaction(balanceBefore)
                .balanceAfterTransaction(account.getTotalBalance().add(refundAmount))
                .build();
        transactionRepository.save(refund);

        // Update account balances
        account.setAvailableBalance(account.getAvailableBalance().add(refundAmount));
        account.setReservedBalance(account.getReservedBalance().subtract(refundAmount));
        withdrawableBalanceComponent.recalculateWithdrawableBalance(account);
        accountRepository.save(account);

        // Update reservation
        reservation.setStatus(ReservationStatus.RELEASED);
        reservation.setUsedAmount(transaction.getPrice().multiply(BigDecimal.valueOf(fulfilledQuantity)));
        reservationRepository.save(reservation);

        log.info("Buy order (partial) cancelled for account {}. {} refunded. Fulfilled: {}, Cancelled: {}",
                account.getId(), refundAmount, fulfilledQuantity, remainingQuantity);
    }

    private Transaction getTransactionBuOrderId(Long orderId) {
        Transaction transaction = transactionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found for order ID: " + orderId));
        return transaction;
    }

    @Override
    @Transactional
    public void sellStock(SellStockRequest request) {
        Account account = findAccount(request.getAccountId());
        Stock stock = stockRepository.findByAccountIdAndSymbol(account.getId(), request.getSymbol())
                .orElseThrow(() -> new StockNotFoundException("Stock not found in account"));

        if (stock.getQuantity() < request.getQuantity()) {
            throw new IllegalArgumentException("Insufficient stock quantity. Available: "
                    + stock.getQuantity() + ", Requested: " + request.getQuantity());
        }

        BigDecimal totalProceeds = request.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        // Reserve stock quantity
        StockReservation stockReservation = StockReservation.builder()
                .accountId(account.getId())
                .orderId(request.getOrderId())
                .symbol(request.getSymbol())
                .reservedQuantity(request.getQuantity())
                .reservationType(ReservationType.SELL_ORDER)
                .status(ReservationStatus.ACTIVE)
                .usedQuantity(0)
                .reservationDate(LocalDateTime.now())
                .description("Reserved stock for sell order")
                .build();
        stockReservationRepository.save(stockReservation);

        // Decrease stock quantity
        stock.setQuantity(stock.getQuantity() - request.getQuantity());
        if (stock.getQuantity() <= 0) {
            stockRepository.delete(stock);
        } else {
            stockRepository.save(stock);
        }

        // Create transaction (PENDING)
        Transaction transaction = transactionMapper.fromSellRequest(request);
        transaction.setTradeDate(LocalDateTime.now());
        transaction.setSettlementDate(LocalDateTime.now().plusDays(2)); // T+2
        transaction.setSettlementStatus(SettlementStatus.PENDING);
        transaction.setTransactionStatus(TransactionStatus.PENDING);
        transaction.setAmount(totalProceeds);
        transaction.setQuantity(request.getQuantity());
        transaction.setBalanceBeforeTransaction(account.getTotalBalance());
        transaction.setBalanceAfterTransaction(account.getTotalBalance());
        transactionRepository.save(transaction);

        // Create withdrawal restriction
        WithdrawalRestriction restriction = WithdrawalRestriction.builder()
                .accountId(account.getId())
                .orderId(request.getOrderId())
                .tradeDate(transaction.getTradeDate().toLocalDate())
                .settlementDate(transaction.getSettlementDate())
                .restrictedAmount(BigDecimal.ZERO)
                .restrictionType(RestrictionType.SELL_PROCEEDS)
                .status(RestrictionStatus.ACTIVE)
                .transactionCount(1)
                .description("Sell proceeds locked for T+2 (OrderId=" + request.getOrderId() + ")")
                .build();
        restrictionRepository.save(restriction);

        log.info("Sell order placed and stock reserved for account {}: {} x {} at {} (orderId={})",
                account.getId(), request.getQuantity(), request.getSymbol(), request.getPrice(), request.getOrderId());
    }

    @Override
    @Transactional
    public void cancelSell(Long orderId) {
        Transaction transaction = getTransactionBuOrderId(orderId);

        // Sadece PENDING veya PARTIALLY_SETTLED durumundaki işlemler iptal edilebilir
        if (transaction.getTransactionStatus() != TransactionStatus.PENDING &&
                transaction.getTransactionStatus() != TransactionStatus.PARTIALLY_SETTLED) {
            throw new OrderAlreadySettledException("Sell order {} already confirmed or cancelled with Order Id:"+ orderId);
         }

        Account account = findAccount(transaction.getAccountId());

        // Transaction durumunu güncelle
        transaction.setTransactionStatus(TransactionStatus.FAILED);
        transaction.setSettlementStatus(SettlementStatus.FAILED);
        transactionRepository.save(transaction);

        // StockReservation geri alınmalı
        StockReservation stockReservation = stockReservationRepository.findByAccountIdAndOrderId(account.getId(), orderId)
                .orElseThrow(() -> new RuntimeException("Stock reservation not found"));

        Stock stock = stockRepository.findByAccountIdAndSymbol(account.getId(), stockReservation.getSymbol())
                .orElse(Stock.builder()
                        .accountId(account.getId())
                        .symbol(stockReservation.getSymbol())
                        .quantity(0)
                        .averageCost(BigDecimal.ZERO)
                        .build());

        stock.setQuantity(stock.getQuantity()+stockReservation.getReservedQuantity()-stockReservation.getUsedQuantity());
        stockRepository.save(stock);

        stockReservation.setStatus(ReservationStatus.RELEASED);
        stockReservation.setUsedQuantity(transaction.getFulfilledQuantity());
        stockReservationRepository.save(stockReservation);

        log.info("Sell order cancelled for account {}. Stock reservation restored. OrderId={}",
                account.getId(), orderId);
    }


    @Override
    @Transactional
    public void confirmSell(Long orderId) {
        Transaction transaction = getTransactionBuOrderId(orderId);

        if (transaction.getTransactionStatus() != TransactionStatus.PENDING &&
                transaction.getTransactionStatus() != TransactionStatus.PARTIALLY_SETTLED) {
            throw new OrderAlreadySettledException("Sell order {} already confirmed or cancelled with Order Id:"+ orderId);
        }

        Account account = findAccount(transaction.getAccountId());

        int fulfilledQuantity = transaction.getQuantity() - transaction.getFulfilledQuantity();
        if (fulfilledQuantity <= 0) {
            log.warn("Sell order {} has no remaining quantity to confirm.", orderId);
            return;
        }

        BigDecimal proceeds = transaction.getPrice().multiply(BigDecimal.valueOf(fulfilledQuantity));

        // Update Transaction
        transaction.setTransactionStatus(TransactionStatus.SETTLED);
        transaction.setSettlementStatus(SettlementStatus.SETTLED);
        transaction.setSettlementDate(LocalDateTime.now());
        transaction.setFulfilledQuantity(transaction.getQuantity()); // tamamı gerçekleşmiş
        transaction.setBalanceAfterTransaction(account.getTotalBalance().add(proceeds));
        transactionRepository.save(transaction);

        // Update Account
        account.setTotalBalance(account.getTotalBalance().add(proceeds));
        withdrawableBalanceComponent.recalculateWithdrawableBalance(account);
        accountRepository.save(account);
        WithdrawalRestriction restriction=restrictionRepository.findByOrderId((orderId)).orElseThrow(()->new RestrictionNotFoundException("Restriction not found for order ID: " + orderId));
        restriction.setRestrictedAmount(transaction.getPrice().multiply(BigDecimal.valueOf(transaction.getQuantity())));

        log.info("Sell order confirmed for account {}. Fulfilled quantity: {}, Proceeds {} settled.",
                account.getId(), fulfilledQuantity, proceeds);
    }



    @Override
    public List<StockDto> getStocksByAccountId(Long accountId) {
        log.info("Fetching all stocks for accountId: {}", accountId);

        List<Stock> stocks = stockRepository.findByAccountId(accountId);
        List<StockDto> stockDtos = stockMapper.toStockDtoList(stocks);

        log.debug("Found {} stock(s) for accountId: {}", stockDtos.size(), accountId);
        return stockDtos;
    }
    @Override
    public StockDto getStockByAccountIdAndSymbol(Long accountId, String symbol) {
        log.info("Fetching stock for accountId: {}, symbol: {}", accountId, symbol);

        return stockRepository.findByAccountIdAndSymbol(accountId, symbol)
                .map(stock -> {
                    log.debug("Stock found for accountId: {}, symbol: {}", accountId, symbol);
                    return stockMapper.toStockDto(stock);
                })
                .orElseGet(() -> {
                    log.warn("No stock found for accountId: {}, symbol: {}", accountId, symbol);
                    return null;
                });
    }
    @Override
    @Transactional
    public void confirmBuyPartially(Long orderId, int fulfilledQuantity) {

        Transaction transaction = getTransactionBuOrderId(orderId);
        if (transaction.getTransactionStatus() != TransactionStatus.PENDING&&transaction.getTransactionStatus() != TransactionStatus.PARTIALLY_SETTLED) {
            log.warn("Order {} already confirmed or cancelled", orderId);
            return;
        }
        Account account = findAccount(transaction.getAccountId());
        BigDecimal totalCost = transaction.getPrice().multiply(BigDecimal.valueOf(fulfilledQuantity));

        if (totalCost.compareTo(account.getAvailableBalance()) > 0) {
            throw new NotEnoughBalanceException("Not enough balance");
        }

        if (fulfilledQuantity <= 0 || fulfilledQuantity + transaction.getFulfilledQuantity() > transaction.getQuantity()) {
            throw new IllegalArgumentException("Invalid fulfilled quantity for order: " + orderId);
        }

        BigDecimal fulfilledAmount = transaction.getPrice().multiply(BigDecimal.valueOf(fulfilledQuantity));

        // Update Transaction
        int newFulfilledQuantity = transaction.getFulfilledQuantity() + fulfilledQuantity;
        transaction.setFulfilledQuantity(newFulfilledQuantity);

        if (newFulfilledQuantity == transaction.getQuantity()) {
            transaction.setTransactionStatus(TransactionStatus.SETTLED);
            transaction.setSettlementStatus(SettlementStatus.SETTLED);
        } else {
            transaction.setTransactionStatus(TransactionStatus.PARTIALLY_SETTLED);
            transaction.setSettlementStatus(SettlementStatus.PARTIALLY_SETTLED);
        }

        transaction.setSettlementDate(LocalDateTime.now());
        transaction.setBalanceAfterTransaction(account.getTotalBalance().subtract(fulfilledAmount));
        transactionRepository.save(transaction);

        // Update Stock
        Stock stock = stockRepository.findByAccountIdAndSymbol(account.getId(), transaction.getSymbol())
                .orElse(Stock.builder()
                        .accountId(account.getId())
                        .symbol(transaction.getSymbol())
                        .quantity(0)
                        .averageCost(BigDecimal.ZERO)
                        .build());

        int currentQty = stock.getQuantity();
        int totalQty = currentQty + fulfilledQuantity;

        BigDecimal totalCostBefore = stock.getAverageCost().multiply(BigDecimal.valueOf(currentQty));
        BigDecimal totalCostAfter = totalCostBefore.add(fulfilledAmount);

        BigDecimal newAverageCost = totalQty > 0
                ? totalCostAfter.divide(BigDecimal.valueOf(totalQty), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        stock.setQuantity(totalQty);
        stock.setAverageCost(newAverageCost);
        stockRepository.save(stock);

        // Update Account
        account.setTotalBalance(account.getTotalBalance().subtract(fulfilledAmount));
        account.setReservedBalance(account.getReservedBalance().subtract(fulfilledAmount));
        withdrawableBalanceComponent.recalculateWithdrawableBalance(account);
        accountRepository.save(account);

        // Update BalanceReservation
        BalanceReservation reservation = reservationRepository.findByAccountIdAndOrderId(account.getId(), orderId)
                .orElseThrow(() -> new BalanceReservationNotFound("Reservation not found for order ID: " + orderId));
        reservation.setStatus(newFulfilledQuantity == transaction.getQuantity()
                ? ReservationStatus.RELEASED
                : ReservationStatus.PARTIALLY_USED);
        reservation.setUsedAmount(reservation.getUsedAmount().add(fulfilledAmount));
        reservationRepository.save(reservation);

        log.info("Buy order (partial) confirmed for account {}: fulfilled {} / {} for symbol {}",
                account.getId(), newFulfilledQuantity, transaction.getQuantity(), transaction.getSymbol());
    }

    @Override
    @Transactional
    public void confirmSellPartially(Long orderId, int fulfilledQuantity) {
        Transaction transaction = getTransactionBuOrderId(orderId);

        if (transaction.getTransactionStatus() != TransactionStatus.PENDING &&
                transaction.getTransactionStatus() != TransactionStatus.PARTIALLY_SETTLED) {
            throw new OrderAlreadySettledException("Sell order {} already confirmed or cancelled with Order Id:"+ orderId);
         }

        Account account = findAccount(transaction.getAccountId());

        int remainingQuantity = transaction.getQuantity() - transaction.getFulfilledQuantity();
        if (fulfilledQuantity <= 0 || fulfilledQuantity > remainingQuantity) {
            throw new IllegalArgumentException("Invalid fulfilled quantity: " + fulfilledQuantity +
                    " for orderId: " + orderId + ", remaining: " + remainingQuantity);
        }

        BigDecimal proceeds = transaction.getPrice().multiply(BigDecimal.valueOf(fulfilledQuantity));

        // Update fulfilledQuantity
        int updatedFulfilled = transaction.getFulfilledQuantity() + fulfilledQuantity;
        transaction.setFulfilledQuantity(updatedFulfilled);

        // Update Status
        if (updatedFulfilled == transaction.getQuantity()) {
            transaction.setTransactionStatus(TransactionStatus.SETTLED);
            transaction.setSettlementStatus(SettlementStatus.SETTLED);
        } else {
            transaction.setTransactionStatus(TransactionStatus.PARTIALLY_SETTLED);
            transaction.setSettlementStatus(SettlementStatus.PARTIALLY_SETTLED);
        }

        transaction.setSettlementDate(LocalDateTime.now());
        transaction.setBalanceAfterTransaction(account.getTotalBalance().add(proceeds));
        transactionRepository.save(transaction);
        WithdrawalRestriction restriction=restrictionRepository.findByOrderId((orderId)).orElseThrow(()->new RestrictionNotFoundException("Restriction not found for order ID: " + orderId));
        restriction.setRestrictedAmount(restriction.getRestrictedAmount().add(proceeds));

        // Update Account
        account.setTotalBalance(account.getTotalBalance().add(proceeds));
        withdrawableBalanceComponent.recalculateWithdrawableBalance(account);
        accountRepository.save(account);

        // Update Stock reservation
        StockReservation stockReservation = stockReservationRepository.findByAccountIdAndOrderId(account.getId(), orderId)
                .orElseThrow(() -> new RuntimeException("Stock reservation not found"));
        stockReservation.setUsedQuantity(fulfilledQuantity);
        stockReservationRepository.save(stockReservation);
        log.info("Sell order partially confirmed for account {}. Fulfilled {} of {}. Proceeds: {}",
                account.getId(), updatedFulfilled, transaction.getQuantity(), proceeds);
    }


    private Account findAccount(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + id));
    }
    @Override
    @Transactional
    public void transferStock(TransferStockRequest request) {
        Account fromAccount = findAccount(request.getFromAccountId());
        Account toAccount = findAccount(request.getToAccountId());

        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new IllegalArgumentException("Cannot transfer stock to the same account");
        }

        Stock fromStock = stockRepository.findByAccountIdAndSymbol(fromAccount.getId(), request.getSymbol())
                .orElseThrow(() -> new StockNotFoundException("Source account does not own the stock"));

        if (fromStock.getQuantity() < request.getQuantity()) {
            throw new IllegalArgumentException("Insufficient stock quantity in source account");
        }

        // 1. Update source stock
        fromStock.setQuantity(fromStock.getQuantity() - request.getQuantity());
        if (fromStock.getQuantity() == 0) {
            stockRepository.delete(fromStock);
        } else {
            stockRepository.save(fromStock);
        }

        // 2. Update destination stock
        Stock toStock = stockRepository.findByAccountIdAndSymbol(toAccount.getId(), request.getSymbol())
                .orElse(Stock.builder()
                        .accountId(toAccount.getId())
                        .symbol(request.getSymbol())
                        .quantity(0)
                        .averageCost(BigDecimal.ZERO)
                        .build());

        toStock.setQuantity(toStock.getQuantity() + request.getQuantity());
        stockRepository.save(toStock);

        Transaction outTxn = Transaction.builder()
                .accountId(fromAccount.getId())
                .symbol(request.getSymbol())
                .transactionType(TransactionType.TRANSFER_OUT)
                .transactionStatus(TransactionStatus.SETTLED)
                .settlementStatus(SettlementStatus.SETTLED)
                .quantity(request.getQuantity())
                .fulfilledQuantity(request.getQuantity())
                .tradeDate(LocalDateTime.now())
                .settlementDate(LocalDateTime.now())
                .description(request.getDescription() != null ? request.getDescription() : "Transferred stock to account " + toAccount.getId())
                .build();
        transactionRepository.save(outTxn);

        Transaction inTxn = Transaction.builder()
                .accountId(toAccount.getId())
                .symbol(request.getSymbol())
                .transactionType(TransactionType.TRANSFER_IN)
                .transactionStatus(TransactionStatus.SETTLED)
                .settlementStatus(SettlementStatus.SETTLED)
                .quantity(request.getQuantity())
                .fulfilledQuantity(request.getQuantity())
                .tradeDate(LocalDateTime.now())
                .settlementDate(LocalDateTime.now())
                .description(request.getDescription() != null ? request.getDescription() : "Received stock from account " + fromAccount.getId())
                .build();
        transactionRepository.save(inTxn);

        log.info("Transferred {} x {} from account {} to account {}",
                request.getQuantity(), request.getSymbol(), fromAccount.getId(), toAccount.getId());
    }

}
