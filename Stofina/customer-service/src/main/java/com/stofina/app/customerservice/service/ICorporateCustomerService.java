package com.stofina.app.customerservice.service;

import com.stofina.app.customerservice.dto.CorporateCustomerDto;
import com.stofina.app.customerservice.request.corporatecustomer.CreateCorporateCustomerRequest;
import com.stofina.app.customerservice.request.corporatecustomer.UpdateCorporateCustomerRequest;

import java.util.List;

/**
 * Service interface for managing Corporate Customers,
 * each of which is linked to a base Customer entity.
 */
public interface ICorporateCustomerService {

    /**
     * Registers a new corporate customer (after base Customer creation).
     ** @param request    corporate-specific fields
     * @return full DTO representation of the new corporate customer
     */
    CorporateCustomerDto create( CreateCorporateCustomerRequest request);

    /**
     * Updates a corporate customer by customer ID.
     *
     * @param customerId ID of the base Customer
     * @param request    new data to apply
     * @return updated DTO
     */
    CorporateCustomerDto update(Long customerId, UpdateCorporateCustomerRequest request);

    /**
     * Deletes the corporate customer by customer ID.
     *
     * @param customerId ID of the base Customer
     */
    void delete(Long customerId);

    /**
     * Retrieves corporate customer details by customer ID.
     *
     * @param customerId ID of the base Customer
     * @return full DTO of corporate customer
     */
    CorporateCustomerDto getById(Long customerId);

    /**
     * Lists all corporate customers in the system.
     *
     * @return list of DTOs
     */
    List<CorporateCustomerDto> getAll();
}
