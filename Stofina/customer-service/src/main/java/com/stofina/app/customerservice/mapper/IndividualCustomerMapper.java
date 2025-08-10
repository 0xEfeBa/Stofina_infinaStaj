package com.stofina.app.customerservice.mapper;

import com.stofina.app.customerservice.dto.IndividualCustomerDto;
import com.stofina.app.customerservice.model.IndividualCustomer;

import com.stofina.app.customerservice.request.individualcustomer.CreateIndividualCustomerRequest;
import com.stofina.app.customerservice.request.individualcustomer.UpdateIndividualCustomerRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = CustomerMapper.class)
public interface IndividualCustomerMapper {

    IndividualCustomerMapper INDIVIDUAL_CUSTOMER_MAPPER = Mappers.getMapper(IndividualCustomerMapper.class);

    IndividualCustomerDto toIndividualCustomerDto(IndividualCustomer individual);

    List<IndividualCustomerDto> toIndividualCustomerDtoList(List<IndividualCustomer> list);

    IndividualCustomer createIndividualCustomer(CreateIndividualCustomerRequest request);

    void updateIndividualCustomer(@MappingTarget IndividualCustomer individual, UpdateIndividualCustomerRequest request);
}
