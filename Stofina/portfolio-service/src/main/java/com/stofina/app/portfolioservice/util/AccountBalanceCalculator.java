package com.stofina.app.portfolioservice.util;

import com.stofina.app.portfolioservice.enums.RestrictionStatus;
import com.stofina.app.portfolioservice.model.Account;
import com.stofina.app.portfolioservice.model.WithdrawalRestriction;
import com.stofina.app.portfolioservice.repository.WithdrawalRestrictionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class AccountBalanceCalculator {

    private final WithdrawalRestrictionRepository restrictionRepository;

    public BigDecimal computeActiveRestrictedTotal(Long accountId) {
        return restrictionRepository.findByAccountIdAndStatus(accountId, RestrictionStatus.ACTIVE)
                .stream()
                .map(WithdrawalRestriction::getRestrictedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public void recalculateWithdrawableBalance(Account account) {
        BigDecimal activeRestrictions = restrictionRepository
                .findByAccountIdAndStatus(account.getId(), RestrictionStatus.ACTIVE)
                .stream()
                .map(WithdrawalRestriction::getRestrictedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal newWithdrawable = account.getTotalBalance()
                .subtract(account.getReservedBalance())
                .subtract(activeRestrictions);


        account.setWithdrawableBalance(newWithdrawable.max(BigDecimal.ZERO));
    }
}
