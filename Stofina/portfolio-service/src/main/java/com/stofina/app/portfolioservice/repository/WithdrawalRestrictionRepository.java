package com.stofina.app.portfolioservice.repository;

import com.stofina.app.portfolioservice.enums.RestrictionStatus;
import com.stofina.app.portfolioservice.model.WithdrawalRestriction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WithdrawalRestrictionRepository extends JpaRepository<WithdrawalRestriction, Long> {

    List<WithdrawalRestriction> findByAccountId(Long accountId);

    List<WithdrawalRestriction> findByStatusAndSettlementDateBefore(RestrictionStatus status, LocalDateTime time);

    List<WithdrawalRestriction> findAllByStatusAndSettlementDateBefore(RestrictionStatus status, LocalDateTime dateTime);
    List<WithdrawalRestriction> findByAccountIdAndStatus(Long accountId, RestrictionStatus status);

    Optional<WithdrawalRestriction> findByOrderId(Long orderId);
}
