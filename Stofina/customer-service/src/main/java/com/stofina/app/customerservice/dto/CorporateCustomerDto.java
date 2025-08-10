package com.stofina.app.customerservice.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorporateCustomerDto {

    private Long id;

    private String tradeName;
    private String tradeRegistryNumber;
    private String taxNumber;
    private String taxOffice;

    private String representativeName;
    private String representativeTckn;
    private String representativePhone;
    private String representativeEmail;

    private CustomerDto customer;
}
