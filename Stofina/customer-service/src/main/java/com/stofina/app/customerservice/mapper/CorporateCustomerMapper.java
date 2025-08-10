package com.stofina.app.customerservice.mapper;

import com.stofina.app.customerservice.dto.CorporateCustomerDto;
import com.stofina.app.customerservice.model.CorporateCustomer;
import com.stofina.app.customerservice.request.corporatecustomer.CreateCorporateCustomerRequest;
import com.stofina.app.customerservice.request.corporatecustomer.UpdateCorporateCustomerRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = CustomerMapper.class)
public interface CorporateCustomerMapper {

    CorporateCustomerMapper CORPORATE_CUSTOMER_MAPPER = Mappers.getMapper(CorporateCustomerMapper.class);

    CorporateCustomerDto toCorporateCustomerDto(CorporateCustomer corporate);

    List<CorporateCustomerDto> toCorporateCustomerDtoList(List<CorporateCustomer> list);

    CorporateCustomer createCorporateCustomer(CreateCorporateCustomerRequest request);

    void updateCorporateCustomer(@MappingTarget CorporateCustomer corporate, UpdateCorporateCustomerRequest request);
}
