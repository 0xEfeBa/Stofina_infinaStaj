package com.stofina.app.customerservice.mapper;

import com.stofina.app.customerservice.dto.CustomerDto;
import com.stofina.app.customerservice.model.Customer;

import com.stofina.app.customerservice.request.customer.CreateCustomerRequest;
import com.stofina.app.customerservice.request.customer.UpdateCustomerRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerMapper CUSTOMER_MAPPER = Mappers.getMapper(CustomerMapper.class);

    CustomerDto toCustomerDto(Customer customer);

    List<CustomerDto> toCustomerDtoList(List<Customer> customerList);

    Customer createCustomer(CreateCustomerRequest request);

    void updateCustomer(@MappingTarget Customer customer, UpdateCustomerRequest request);
}
