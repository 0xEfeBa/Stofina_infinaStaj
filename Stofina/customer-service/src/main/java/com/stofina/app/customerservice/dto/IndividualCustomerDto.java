package com.stofina.app.customerservice.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndividualCustomerDto {

    private Long id;
    private String tckn;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private CustomerDto customer;
}
