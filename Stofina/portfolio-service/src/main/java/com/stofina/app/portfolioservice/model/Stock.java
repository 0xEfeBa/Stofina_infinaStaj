package com.stofina.app.portfolioservice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "account_stocks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder

public class Stock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", referencedColumnName = "id", insertable = false, updatable = false)
    @ToString.Exclude
    private Account account;

    @Column(nullable = false)
    private String symbol;

    private Integer quantity;

    @Column(precision = 19, scale = 4)
    private BigDecimal averageCost;
}
