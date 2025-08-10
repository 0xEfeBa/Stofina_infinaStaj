package com.stofina.app.customerservice.model;

import com.stofina.app.customerservice.enums.CustomerStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Customer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "legal_address", nullable = false, length = 255)
    private String legalAddress;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CustomerStatus status=CustomerStatus.ACTIVE;

}
