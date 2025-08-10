package com.stofina.app.customerservice.request.corporatecustomer;

import com.stofina.app.customerservice.request.customer.UpdateCustomerRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCorporateCustomerRequest {

    @NotBlank(message = "Ticaret unvanı boş olamaz.")
    private String tradeName;

    @NotBlank(message = "Ticaret sicil numarası boş olamaz.")
    private String tradeRegistryNumber;

    @NotBlank(message = "Vergi numarası boş olamaz.")
    private String taxNumber;

    @NotBlank(message = "Vergi dairesi boş olamaz.")
    private String taxOffice;

    @NotBlank(message = "Temsilci adı boş olamaz.")
    private String representativeName;

    @NotBlank(message = "Temsilci TCKN alanı boş olamaz.")
    private String representativeTckn;

    @NotBlank(message = "Temsilci telefon numarası boş olamaz.")
    private String representativePhone;

    @NotBlank(message = "Temsilci e-posta alanı boş olamaz.")
    @Email(message = "Geçerli bir temsilci e-posta adresi giriniz.")
    private String representativeEmail;

    @Valid
    @NotNull(message = "Müşteri bilgileri girilmelidir.")
    private UpdateCustomerRequest customer;
}
