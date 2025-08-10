package com.stofina.app.customerservice.service.impl;

import com.stofina.app.customerservice.exception.CustomerNotFoundException;
import com.stofina.app.customerservice.mapper.CustomerMapper;
import com.stofina.app.customerservice.model.Customer;
import com.stofina.app.customerservice.repository.CustomerRepository;
import com.stofina.app.customerservice.request.customer.CreateCustomerRequest;
import com.stofina.app.customerservice.request.customer.UpdateCustomerRequest;
import com.stofina.app.customerservice.service.ICustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements ICustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public Customer createCustomer(CreateCustomerRequest request) {
        log.info("Creating base customer...");
        Customer customer = customerMapper.createCustomer(request);
        customer = customerRepository.save(customer);
        log.info("Base customer created with ID: {}", customer.getId());
        return customer;
    }

    @Override
    public Customer updateCustomer(Long id, UpdateCustomerRequest request) {
        log.info("Updating customer with ID: {}", id);
        Customer customer = getByIdOrThrow(id);
        customerMapper.updateCustomer(customer, request);
        Customer updated = customerRepository.save(customer);
        log.info("Customer updated. ID: {}", updated.getId());
        return updated;
    }

    @Override
    public void deleteCustomer(Long id) {
        log.info("Deleting customer with ID: {}", id);
        Customer customer = getByIdOrThrow(id);
        customerRepository.delete(customer);
        log.info("Customer deleted. ID: {}", id);
    }

    @Override
    public Customer getCustomerById(Long id) {
        log.info("Fetching customer by ID: {}", id);
        return getByIdOrThrow(id);
    }

    @Override
    public List<Customer> getAllCustomers() {
        log.info("Fetching all customers...");
        return customerRepository.findAll();
    }

    private Customer getByIdOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Customer not found: {}", id);
                    return new CustomerNotFoundException("Customer not found with id: " + id);
                });
    }
}
