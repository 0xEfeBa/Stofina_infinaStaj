package com.stofina.app.customerservice.service.impl;

import com.stofina.app.customerservice.dto.CorporateCustomerDto;
import com.stofina.app.customerservice.exception.CorporateCustomerNotFoundException;
import com.stofina.app.customerservice.mapper.CorporateCustomerMapper;
import com.stofina.app.customerservice.model.CorporateCustomer;
import com.stofina.app.customerservice.model.Customer;
import com.stofina.app.customerservice.repository.CorporateCustomerRepository;
import com.stofina.app.customerservice.request.corporatecustomer.CreateCorporateCustomerRequest;
import com.stofina.app.customerservice.request.corporatecustomer.UpdateCorporateCustomerRequest;
import com.stofina.app.customerservice.service.ICorporateCustomerService;
import com.stofina.app.customerservice.service.ICustomerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CorporateCustomerServiceImpl implements ICorporateCustomerService {

    private final CorporateCustomerRepository corporateRepo;
    private final ICustomerService customerService;
    private final CorporateCustomerMapper corporateMapper;

    @Override
    @Transactional
    public CorporateCustomerDto create(CreateCorporateCustomerRequest request) {
        Customer customer=customerService.createCustomer(request.getCustomer());
        log.info("Creating corporate customer for customerId={}", customer.getId());
        CorporateCustomer corporate = corporateMapper.createCorporateCustomer(request);
        corporate.setCustomer(customer);
        corporate = corporateRepo.save(corporate);
        return corporateMapper.toCorporateCustomerDto(corporate);
    }

    @Override
    @Transactional
    public CorporateCustomerDto update(Long customerId, UpdateCorporateCustomerRequest request) {
        log.info("Updating corporate customer for customerId={}", customerId);
        CorporateCustomer corporate = getCorporateOrThrow(customerId);
        corporateMapper.updateCorporateCustomer(corporate, request);
        corporate = corporateRepo.save(corporate);
        return corporateMapper.toCorporateCustomerDto(corporate);
    }

    @Override
    @Transactional
    public void delete(Long customerId) {
        log.info("Deleting corporate customer for customerId={}", customerId);
        CorporateCustomer corporate = getCorporateOrThrow(customerId);
        corporateRepo.delete(corporate);
    }

    @Override
    public CorporateCustomerDto getById(Long customerId) {
        log.info("Fetching corporate customer by customerId={}", customerId);
        return corporateMapper.toCorporateCustomerDto(getCorporateOrThrow(customerId));
    }

    @Override
    public List<CorporateCustomerDto> getAll() {
        log.info("Fetching all corporate customers...");
        return corporateMapper.toCorporateCustomerDtoList(corporateRepo.findAll());
    }

    private CorporateCustomer getCorporateOrThrow(Long id) {
        return corporateRepo.findById(id).orElseThrow(() -> {
            log.warn("Corporate customer not found: {}", id);
            return new CorporateCustomerNotFoundException("Corporate customer not found with id: " + id);
        });
    }
}
