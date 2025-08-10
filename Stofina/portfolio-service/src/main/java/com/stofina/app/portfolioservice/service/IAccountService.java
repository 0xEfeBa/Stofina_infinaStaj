package com.stofina.app.portfolioservice.service;

import com.stofina.app.portfolioservice.dto.AccountDto;
import com.stofina.app.portfolioservice.dto.BalanceDto;
import com.stofina.app.portfolioservice.dto.BalanceReservationDto;
import com.stofina.app.portfolioservice.dto.WithdrawableBalanceDto;
import com.stofina.app.portfolioservice.request.account.*;
import jakarta.validation.Valid;

import java.util.List;

public interface IAccountService {

    /**
     * Creates a new investment account with the given request data.
     *
     * @param request the account creation request containing initial balance and customer info
     * @return the created account DTO
     */
    AccountDto createAccount(CreateAccountRequest request);

    /**
     * Retrieves an account by its ID, including its current stocks.
     *
     * @param accountId the account ID
     * @return the account DTO
     */
    AccountDto getAccountById(Long accountId);

    /**
     * Retrieves all accounts owned by a specific customer.
     *
     * @param customerId the customer ID
     * @return list of account DTOs
     */
    List<AccountDto> getAccountsByCustomerId(Long customerId);

    /**
     * Deposits money into the specified account.
     *
     * @param accountId the account ID
     * @param request   the deposit amount and optional description
     */
    void deposit(Long accountId, DepositRequest request);

    /**
     * Withdraws money from the specified account, if available.
     *
     * @param accountId the account ID
     * @param request   the withdrawal amount and optional description
     */
    void withdraw(Long accountId, WithdrawRequest request);

    /**
     * Changes the status of an account (e.g., ACTIVE → SUSPENDED).
     *
     * @param accountId the account ID
     * @param request   the status change request
     * @return AccountDto
     */
    AccountDto updateStatus(Long accountId, PatchAccountStatusRequest request);

    /**
     * Retrieves full balance details of the account.
     *
     * @param accountId the account ID
     * @return balance information as BalanceDto
     */
    BalanceDto getBalanceByAccountId(Long accountId);

    /**
     * Retrieves the currently withdrawable balance from the account.
     *
     * @param accountId the account ID
     * @return withdrawable balance as WithdrawableBalanceDto
     */
    WithdrawableBalanceDto getWithdrawableBalance(Long accountId);

    /**
     * Retrieves all active reservations (locked funds) for the given account.
     *
     * @param accountId the account ID
     * @return list of BalanceReservationDto
     */
    List<BalanceReservationDto> getActiveReservationsByAccountId(Long accountId);

    /**
     * Transfers money from one account to another.
     *
     * @param request the transfer details including source, destination, and amount
     */
    void transferMoney(TransferMoneyRequest request);
}


