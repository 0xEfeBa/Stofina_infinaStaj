package com.stofina.app.portfolioservice.scheduler;

import com.stofina.app.portfolioservice.enums.RestrictionStatus;
import com.stofina.app.portfolioservice.exception.AccountNotFoundException;
import com.stofina.app.portfolioservice.model.Account;
import com.stofina.app.portfolioservice.model.WithdrawalRestriction;
import com.stofina.app.portfolioservice.repository.AccountRepository;
import com.stofina.app.portfolioservice.repository.WithdrawalRestrictionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TPlus2Job {

    private final WithdrawalRestrictionRepository restrictionRepository;
    private final AccountRepository accountRepository;


    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void releaseSettledRestrictions() {
        LocalDateTime now = LocalDateTime.now();

        List<WithdrawalRestriction> restrictions = restrictionRepository
                .findAllByStatusAndSettlementDateBefore(RestrictionStatus.ACTIVE, now);

        for (WithdrawalRestriction restriction : restrictions) {
            Account account = accountRepository.findById(restriction.getAccountId())
                    .orElseThrow(() -> new AccountNotFoundException("Account not found for restriction ID: " + restriction.getId()));

            account.setWithdrawableBalance(
                    account.getWithdrawableBalance().add(restriction.getRestrictedAmount())
            );
            restriction.setStatus(RestrictionStatus.RELEASED);

            accountRepository.save(account);
            restrictionRepository.save(restriction);

            log.info("T+2 restriction released for account {}: {} added to available balance (restrictionId={})",
                    account.getId(), restriction.getRestrictedAmount(), restriction.getId());
        }
    }
}
