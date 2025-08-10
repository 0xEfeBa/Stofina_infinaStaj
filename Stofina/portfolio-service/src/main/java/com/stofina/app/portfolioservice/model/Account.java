package com.stofina.app.portfolioservice.model;

import com.stofina.app.portfolioservice.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Account extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;

    @Column(unique = true, nullable = false)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    @Column(precision = 19, scale = 4)
    private BigDecimal totalBalance;

    @Column(precision = 19, scale = 4)
    private BigDecimal availableBalance;

    @Column(precision = 19, scale = 4)
    private BigDecimal reservedBalance;

    @Column(precision = 19, scale = 4)
    private BigDecimal withdrawableBalance;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Stock> stocks = new ArrayList<>();

}
