package com.stofina.app.customerservice.service.impl;

import com.stofina.app.customerservice.dto.IndividualCustomerDto;
import com.stofina.app.customerservice.exception.IndividualCustomerNotFoundException;
import com.stofina.app.customerservice.mapper.IndividualCustomerMapper;
import com.stofina.app.customerservice.model.Customer;
import com.stofina.app.customerservice.model.IndividualCustomer;
import com.stofina.app.customerservice.repository.CustomerRepository;
import com.stofina.app.customerservice.repository.IndividualCustomerRepository;
import com.stofina.app.customerservice.request.individualcustomer.CreateIndividualCustomerRequest;
import com.stofina.app.customerservice.request.individualcustomer.UpdateIndividualCustomerRequest;
import com.stofina.app.customerservice.service.ICustomerService;
import com.stofina.app.customerservice.service.IIndividualCustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndividualCustomerServiceImpl implements IIndividualCustomerService {

    private final IndividualCustomerRepository individualRepo;
    private final ICustomerService customerService;
    private final IndividualCustomerMapper individualMapper;

    @Override
    public IndividualCustomerDto create( CreateIndividualCustomerRequest request) {
        Customer customer=customerService.createCustomer(request.getCustomer());
        log.info("Creating individual customer for customerId={}", customer.getId());
        IndividualCustomer individual = individualMapper.createIndividualCustomer(request);
        individual.setCustomer(customer);
        individual = individualRepo.save(individual);
        return individualMapper.toIndividualCustomerDto(individual);
    }


    @Override
    public IndividualCustomerDto update(Long customerId, UpdateIndividualCustomerRequest request) {
        log.info("Updating individual customer for customerId={}", customerId);
        IndividualCustomer individual = getIndividualOrThrow(customerId);
        individualMapper.updateIndividualCustomer(individual, request);
        individual = individualRepo.save(individual);
        return individualMapper.toIndividualCustomerDto(individual);
    }

    @Override
    public void delete(Long customerId) {
        log.info("Deleting individual customer for customerId={}", customerId);
        IndividualCustomer individual = getIndividualOrThrow(customerId);
        individualRepo.delete(individual);
    }

    @Override
    public IndividualCustomerDto getById(Long customerId) {
        log.info("Fetching individual customer by customerId={}", customerId);
        return individualMapper.toIndividualCustomerDto(getIndividualOrThrow(customerId));
    }

    @Override
    public List<IndividualCustomerDto> getAll() {
        log.info("Fetching all individual customers...");
        return individualMapper.toIndividualCustomerDtoList(individualRepo.findAll());
    }

    private IndividualCustomer getIndividualOrThrow(Long id) {
        return individualRepo.findById(id).orElseThrow(() -> {
            log.warn("Individual customer not found: {}", id);
            return new IndividualCustomerNotFoundException("Individual customer not found with id: " + id);
        });
    }
}
