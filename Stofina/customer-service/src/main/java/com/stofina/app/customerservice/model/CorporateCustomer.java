package com.stofina.app.customerservice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "corporate_customers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_corp_trade_registry", columnNames = "trade_registry_number"),
                @UniqueConstraint(name = "uk_corp_tax_number", columnNames = "tax_number"),
                @UniqueConstraint(name = "uk_corp_rep_tckn", columnNames = "representative_tckn"),
                @UniqueConstraint(name = "uk_corp_rep_email", columnNames = "representative_email")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CorporateCustomer extends BaseEntity{

    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Customer customer;

    @Column(name = "trade_name", nullable = false, length = 150)
    private String tradeName;

    @Column(name = "trade_registry_number", nullable = false, unique = true, length = 50)
    private String tradeRegistryNumber;

    @Column(name = "tax_number", nullable = false, unique = true, length = 20)
    private String taxNumber;

    @Column(name = "tax_office", nullable = false, length = 100)
    private String taxOffice;

    @Column(name = "representative_name", nullable = false, length = 100)
    private String representativeName;

    @Column(name = "representative_tckn", nullable = false, unique = true, length = 11)
    private String representativeTckn;

    @Column(name = "representative_phone", nullable = false, length = 20)
    private String representativePhone;

    @Column(name = "representative_email", nullable = false, length = 100)
    private String representativeEmail;
}
