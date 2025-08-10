package com.stofina.app.portfolioservice.service.impl;

import com.stofina.app.portfolioservice.dto.WithdrawalRestrictionDto;
import com.stofina.app.portfolioservice.enums.RestrictionStatus;
import com.stofina.app.portfolioservice.enums.RestrictionType;
import com.stofina.app.portfolioservice.mapper.WithdrawalRestrictionMapper;
import com.stofina.app.portfolioservice.model.Account;
import com.stofina.app.portfolioservice.model.WithdrawalRestriction;
import com.stofina.app.portfolioservice.repository.AccountRepository;
import com.stofina.app.portfolioservice.repository.WithdrawalRestrictionRepository;
import com.stofina.app.portfolioservice.service.IWithdrawalRestrictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WithdrawalRestrictionServiceImpl implements IWithdrawalRestrictionService {

    private final WithdrawalRestrictionRepository restrictionRepository;
    private final AccountRepository accountRepository;
    private final WithdrawalRestrictionMapper mapper;

    @Override
    public void createRestriction(Long accountId, BigDecimal amount, LocalDate tradeDate,
                                  LocalDateTime settlementDate, String description) {
        WithdrawalRestriction restriction = WithdrawalRestriction.builder()
                .accountId(accountId)
                .tradeDate(tradeDate)
                .settlementDate(settlementDate)
                .restrictedAmount(amount)
                .status(RestrictionStatus.ACTIVE)
                .restrictionType(RestrictionType.SELL_PROCEEDS)
                .transactionCount(1)
                .description(description)
                .build();

        restrictionRepository.save(restriction);
        log.info("Created T+2 restriction for account {} amount {} to be released on {}",
                accountId, amount, settlementDate);
    }

    @Override
    public List<WithdrawalRestrictionDto> getActiveRestrictions(Long accountId) {
        return restrictionRepository.findByAccountId(accountId).stream()
                .filter(r -> r.getStatus() == RestrictionStatus.ACTIVE)
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public void expireEligibleRestrictions() {
        LocalDateTime now = LocalDateTime.now();
        List<WithdrawalRestriction> expired = restrictionRepository
                .findByStatusAndSettlementDateBefore(RestrictionStatus.ACTIVE, now);

        for (WithdrawalRestriction r : expired) {
            r.setStatus(RestrictionStatus.EXPIRED);

            Account account = accountRepository.findById(r.getAccountId())
                    .orElseThrow(() -> new IllegalStateException("Account not found"));

            account.setWithdrawableBalance(account.getWithdrawableBalance().add(r.getRestrictedAmount()));

            restrictionRepository.save(r);
            accountRepository.save(account);

            log.info("Expired restriction ID {} - amount {} added to withdrawable balance of account {}",
                    r.getId(), r.getRestrictedAmount(), account.getId());
        }

        log.info("Expired {} T+2 restrictions", expired.size());
    }
    @Override
    public List<WithdrawalRestrictionDto> getRestrictionsByAccountId(Long accountId) {
        List<WithdrawalRestriction> restrictions = restrictionRepository.findByAccountIdAndStatus(accountId, RestrictionStatus.ACTIVE);

        List<WithdrawalRestrictionDto> dtos = restrictions.stream().map(r -> WithdrawalRestrictionDto.builder()
                .id(r.getId())
                .tradeDate(r.getTradeDate())
                .settlementDate(r.getSettlementDate())
                .restrictedAmount(r.getRestrictedAmount())
                .status(r.getStatus())
                .transactionCount(r.getTransactionCount())
                .description(r.getDescription())
                .build()).collect(Collectors.toList());

        log.info("{} active withdrawal restrictions found for account {}", dtos.size(), accountId);
        return dtos;
    }
}
