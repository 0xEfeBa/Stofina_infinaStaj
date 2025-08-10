package com.stofina.app.customerservice.service;

import com.stofina.app.customerservice.dto.IndividualCustomerDto;
import com.stofina.app.customerservice.request.individualcustomer.CreateIndividualCustomerRequest;
import com.stofina.app.customerservice.request.individualcustomer.UpdateIndividualCustomerRequest;


import java.util.List;

/**
 * Service interface for managing Individual Customers,
 * which are associated one-to-one with a base Customer entity.
 */
public interface IIndividualCustomerService {

    /**
     * Registers a new individual customer (after base Customer creation).
     *
     * @param request    individual-specific fields
     * @return full DTO representation of the new individual customer
     */
    IndividualCustomerDto create( CreateIndividualCustomerRequest request);

    /**
     * Updates an individual customer by customer ID.
     *
     * @param customerId ID of the base Customer
     * @param request    new data to apply
     * @return updated DTO
     */
    IndividualCustomerDto update(Long customerId, UpdateIndividualCustomerRequest request);

    /**
     * Deletes the individual customer by customer ID.
     *
     * @param customerId ID of the base Customer
     */
    void delete(Long customerId);

    /**
     * Retrieves individual customer details by customer ID.
     *
     * @param customerId ID of the base Customer
     * @return full DTO of individual customer
     */
    IndividualCustomerDto getById(Long customerId);

    /**
     * Lists all individual customers in the system.
     *
     * @return list of DTOs
     */
    List<IndividualCustomerDto> getAll();
}
