package com.stofina.app.customerservice.repository;

import com.stofina.app.customerservice.model.CorporateCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CorporateCustomerRepository extends JpaRepository<CorporateCustomer, Long> {

    Optional<CorporateCustomer> findByTaxNumber(String taxNumber);

    boolean existsByTaxNumber(String taxNumber);

    boolean existsByRepresentativeTckn(String representativeTckn);
    
    boolean existsByTradeRegistryNumber(String tradeRegistryNumber);

    boolean existsByRepresentativeEmail(String representativeEmail);
}