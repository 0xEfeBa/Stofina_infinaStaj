package com.stofina.app.portfolioservice.service.impl;

import com.stofina.app.portfolioservice.dto.AccountDto;
import com.stofina.app.portfolioservice.dto.BalanceDto;
import com.stofina.app.portfolioservice.dto.BalanceReservationDto;
import com.stofina.app.portfolioservice.dto.WithdrawableBalanceDto;
import com.stofina.app.portfolioservice.enums.*;
import com.stofina.app.portfolioservice.exception.AccountNotFoundException;
import com.stofina.app.portfolioservice.mapper.AccountMapper;
import com.stofina.app.portfolioservice.model.Account;
import com.stofina.app.portfolioservice.model.BalanceReservation;
import com.stofina.app.portfolioservice.model.Transaction;
import com.stofina.app.portfolioservice.repository.AccountRepository;
import com.stofina.app.portfolioservice.repository.BalanceReservationRepository;
import com.stofina.app.portfolioservice.repository.TransactionRepository;
import com.stofina.app.portfolioservice.request.account.*;
import com.stofina.app.portfolioservice.service.IAccountService;
import com.stofina.app.portfolioservice.util.AccountBalanceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements IAccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountMapper accountMapper;
    private final BalanceReservationRepository reservationRepository;
    private final AccountBalanceCalculator balanceCalculator;

    @Override
    public AccountDto createAccount(CreateAccountRequest request) {
        Account account = Account.builder()
                .customerId(request.getCustomerId())
                .totalBalance(request.getInitialBalance())
                .accountNumber(generateUniqueAccountNumber())
                .availableBalance(request.getInitialBalance())
                .reservedBalance(BigDecimal.ZERO)
                .withdrawableBalance(request.getInitialBalance())
                .status(AccountStatus.ACTIVE)
                .build();

        account = accountRepository.save(account);
        log.info("Created new account with ID: {}", account.getId());
        return accountMapper.toAccountDto(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDto getAccountById(Long accountId) {
        Account account = findAccount(accountId);
        return accountMapper.toAccountDto(account);
    }

    @Override
    public List<AccountDto> getAccountsByCustomerId(Long customerId) {
        return accountRepository.findByCustomerId(customerId)
                .stream()
                .map(accountMapper::toAccountDto)
                .toList();
    }

    @Override
    public void deposit(Long accountId, DepositRequest request) {
        Account account = findAccount(accountId);
        BigDecimal amount = request.getAmount();

        account.setTotalBalance(account.getTotalBalance().add(amount));
        account.setAvailableBalance(account.getAvailableBalance().add(amount));
        balanceCalculator.recalculateWithdrawableBalance(account);
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .accountId(accountId)
                .transactionType(TransactionType.DEPOSIT)
                .amount(amount)
                .price(request.getAmount())
                .quantity(0)
                .symbol("TRY")
                .tradeDate(LocalDateTime.now())
                .settlementDate(LocalDateTime.now())
                .settlementStatus(SettlementStatus.SETTLED)
                .balanceBeforeTransaction(account.getTotalBalance().subtract(amount))
                .balanceAfterTransaction(account.getTotalBalance())
                .build();

        transactionRepository.save(transaction);
        log.info("Deposited {} into account ID {}", amount, accountId);
    }

    @Override
    public void withdraw(Long accountId, WithdrawRequest request) {
        Account account = findAccount(accountId);
        BigDecimal amount = request.getAmount();

        if (account.getWithdrawableBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance to withdraw");
        }

        account.setTotalBalance(account.getTotalBalance().subtract(amount));
        account.setAvailableBalance(account.getAvailableBalance().subtract(amount));
        balanceCalculator.recalculateWithdrawableBalance(account);
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .accountId(accountId)
                .transactionType(TransactionType.WITHDRAWAL)
                .amount(amount)
                .price(request.getAmount())
                .quantity(0)
                .symbol("TRY")
                .tradeDate(LocalDateTime.now())
                .settlementDate(LocalDateTime.now())
                .settlementStatus(SettlementStatus.SETTLED)
                .balanceBeforeTransaction(account.getTotalBalance().add(amount))
                .balanceAfterTransaction(account.getTotalBalance())
                .build();

        transactionRepository.save(transaction);
        log.info("Withdrew {} from account ID {}", amount, accountId);
    }

    @Override
    public AccountDto updateStatus(Long accountId, PatchAccountStatusRequest request) {
        Account account = findAccount(accountId);
        account.setStatus(request.getNewStatus());
        accountRepository.save(account);
        log.info("Updated account ID {} status to {}", accountId, request.getNewStatus());
        return accountMapper.toAccountDto(account);
    }
    @Override
    public BalanceDto getBalanceByAccountId(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with ID: " + accountId));
        BigDecimal restricted = balanceCalculator.computeActiveRestrictedTotal(accountId); // 👈 ek

        BalanceDto balanceDto = BalanceDto.builder()
                .totalBalance(account.getTotalBalance())
                .availableBalance(account.getAvailableBalance())
                .reservedBalance(account.getReservedBalance())
                .withdrawableBalance(account.getWithdrawableBalance())
                .restrictedBalance(restricted)
                .build();

        log.info("Balance retrieved for account {}: {}", accountId, balanceDto);
        return balanceDto;
}

    @Override
    public WithdrawableBalanceDto getWithdrawableBalance(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with ID: " + accountId));

        WithdrawableBalanceDto dto = WithdrawableBalanceDto.builder()
                .withdrawableAmount(account.getWithdrawableBalance())
                .note("T+2 restrictions applied")
                .build();

        log.info("Withdrawable balance retrieved for account {}: {}", accountId, dto);
        return dto;
    }
    @Override
    @Transactional
    public void transferMoney( TransferMoneyRequest request) {
        String fromAccountNumber = request.getFromAccountNumber();
        String toAccountNumber = request.getToAccountNumber();
        BigDecimal amount = request.getAmount();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive.");
        }

        Account fromAccount = accountRepository.findByAccountNumber(fromAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException("From account not found: " + fromAccountNumber));

        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException("To account not found: " + toAccountNumber));

        if (fromAccount.getAvailableBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance in source account.");
        }

        // 3. Update source account
        fromAccount.setAvailableBalance(fromAccount.getAvailableBalance().subtract(amount));
        fromAccount.setTotalBalance(fromAccount.getTotalBalance().subtract(amount));
        balanceCalculator.recalculateWithdrawableBalance(fromAccount);

        // 4. Update target account
        toAccount.setAvailableBalance(toAccount.getAvailableBalance().add(amount));
        toAccount.setTotalBalance(toAccount.getTotalBalance().add(amount));
        balanceCalculator.recalculateWithdrawableBalance(toAccount);

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // 5. Record transaction on both sides
        Transaction withdrawTxn = Transaction.builder()
                .accountId(fromAccount.getId())
                .symbol("TRY")
                .transactionType(TransactionType.TRANSFER_OUT)
                .transactionStatus(TransactionStatus.SETTLED)
                .settlementStatus(SettlementStatus.SETTLED)
                .amount(amount.negate())
                .price(BigDecimal.ZERO)
                .quantity(0)
                .tradeDate(LocalDateTime.now())
                .settlementDate(LocalDateTime.now())
                .balanceBeforeTransaction(fromAccount.getTotalBalance().add(amount))
                .balanceAfterTransaction(fromAccount.getTotalBalance())
                .description("Transfer to account " + toAccountNumber + " - " + request.getDescription())
                .build();

        Transaction depositTxn = Transaction.builder()
                .accountId(toAccount.getId())
                .symbol("TRY")
                .transactionType(TransactionType.TRANSFER_IN)
                .transactionStatus(TransactionStatus.SETTLED)
                .settlementStatus(SettlementStatus.SETTLED)
                .amount(amount)
                .price(BigDecimal.ZERO)
                .quantity(0)
                .tradeDate(LocalDateTime.now())
                .settlementDate(LocalDateTime.now())
                .balanceBeforeTransaction(toAccount.getTotalBalance().subtract(amount))
                .balanceAfterTransaction(toAccount.getTotalBalance())
                .description("Transfer from account " + fromAccountNumber + " - " + request.getDescription())
                .build();

        transactionRepository.save(withdrawTxn);
        transactionRepository.save(depositTxn);

        log.info("Transferred {} from account {} to account {}.", amount, fromAccountNumber, toAccountNumber);
    }



    @Override
    public List<BalanceReservationDto> getActiveReservationsByAccountId(Long accountId) {
        List<BalanceReservation> reservations = reservationRepository.findByAccountIdAndStatus(accountId, ReservationStatus.ACTIVE);

        List<BalanceReservationDto> dtos = reservations.stream().map(res -> BalanceReservationDto.builder()
                .id(res.getId())
                .reservedAmount(res.getReservedAmount())
                .status(res.getStatus())
                .usedAmount(res.getUsedAmount())
                .reservationDate(res.getReservationDate())
                .expiryDate(res.getExpiryDate())
                .description(res.getDescription())
                .build()).collect(Collectors.toList());

        log.info("{} active reservations found for account {}", dtos.size(), accountId);
        return dtos;
    }

    private Account findAccount(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + id));
    }
    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            accountNumber = generateRandomAccountNumber();
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }

    private String generateRandomAccountNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

}
