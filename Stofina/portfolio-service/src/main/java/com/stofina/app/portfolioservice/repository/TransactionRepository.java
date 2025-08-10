package com.stofina.app.portfolioservice.repository;

import com.stofina.app.portfolioservice.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByAccountId(Long accountId, Pageable pageable);

    List<Transaction> findByAccountId(Long accountId);
    Optional<Transaction> findByOrderId(Long orderId);
}
