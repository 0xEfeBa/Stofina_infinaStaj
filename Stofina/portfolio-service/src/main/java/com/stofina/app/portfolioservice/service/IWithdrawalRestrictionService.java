package com.stofina.app.portfolioservice.service;

import com.stofina.app.portfolioservice.dto.WithdrawalRestrictionDto;

import java.util.List;

/**
 * Service interface for managing T+2 withdrawal restrictions.
 */
public interface IWithdrawalRestrictionService {

    /**
     * Creates a new withdrawal restriction (typically after a SELL transaction).
     *
     * @param accountId the account ID
     * @param restrictedAmount the amount to restrict
     * @param tradeDate the date of the trade
     * @param settlementDate the T+2 settlement date
     * @param description description of the restriction
     */
    void createRestriction(Long accountId,
                           java.math.BigDecimal restrictedAmount,
                           java.time.LocalDate tradeDate,
                           java.time.LocalDateTime settlementDate,
                           String description);

    /**
     * Returns all ACTIVE withdrawal restrictions for a given account.
     *
     * @param accountId the account ID
     * @return list of active restrictions
     */
    List<WithdrawalRestrictionDto> getActiveRestrictions(Long accountId);

    /**
     * Finds all restrictions that are eligible to expire and marks them as EXPIRED.
     */
    void expireEligibleRestrictions();
    /**
     * Withdrawal restriction service interface for T+2 operations.
     */

    /**
     * Retrieves all active withdrawal restrictions due to T+2 rules for the account.
     *
     * @param accountId the account ID
     * @return list of WithdrawalRestrictionDto
     */
    List<WithdrawalRestrictionDto> getRestrictionsByAccountId(Long accountId);
}
