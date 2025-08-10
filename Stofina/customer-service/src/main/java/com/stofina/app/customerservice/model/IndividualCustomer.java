package com.stofina.app.customerservice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "individual_customers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ind_tckn", columnNames = "tckn"),
                @UniqueConstraint(name = "uk_ind_email", columnNames = "email")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class IndividualCustomer  extends BaseEntity{

    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Customer customer;

    @Column(name = "tckn", nullable = false, unique = true, length = 11)
    private String tckn;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

}