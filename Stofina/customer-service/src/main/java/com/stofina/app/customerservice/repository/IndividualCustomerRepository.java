package com.stofina.app.customerservice.repository;

import com.stofina.app.customerservice.model.IndividualCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IndividualCustomerRepository extends JpaRepository<IndividualCustomer, Long> {

    Optional<IndividualCustomer> findByTckn(String tckn);

    boolean existsByTckn(String tckn);


    boolean existsByEmail(String email);}
