package com.stofina.app.customerservice.dto;

import com.stofina.app.customerservice.enums.CustomerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDto {
    private Long id;
    private String legalAddress;
    private CustomerStatus status;
}
