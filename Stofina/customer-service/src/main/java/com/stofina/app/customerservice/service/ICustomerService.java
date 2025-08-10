package com.stofina.app.customerservice.service;

import com.stofina.app.customerservice.model.Customer;
import com.stofina.app.customerservice.request.customer.CreateCustomerRequest;
import com.stofina.app.customerservice.request.customer.UpdateCustomerRequest;


import java.util.List;

/**
 * Service interface for performing base operations on the Customer entity,
 * which is a shared root for both individual and corporate customers.
 */
public interface ICustomerService {

    /**
     * Creates a new base Customer entity.
     *
     * @param request the request object containing customer data
     * @return the saved Customer entity
     */
    Customer createCustomer(CreateCustomerRequest request);

    /**
     * Updates an existing Customer entity by ID.
     *
     * @param id      the ID of the customer to update
     * @param request the new data to apply
     * @return the updated Customer entity
     */
    Customer updateCustomer(Long id, UpdateCustomerRequest request);

    /**
     * Deletes the base Customer entity by ID.
     *
     * @param id the ID of the customer to delete
     */
    void deleteCustomer(Long id);

    /**
     * Finds a Customer by ID.
     *
     * @param id the customer ID
     * @return the Customer entity
     */
    Customer getCustomerById(Long id);

    /**
     * Retrieves all Customer entities.
     *
     * @return list of all customers
     */
    List<Customer> getAllCustomers();
}
