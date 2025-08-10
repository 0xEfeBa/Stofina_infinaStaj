package com.stofina.app.customerservice.request.individualcustomer;

import com.stofina.app.customerservice.request.customer.UpdateCustomerRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateIndividualCustomerRequest {

    @NotBlank(message = "TCKN alanı boş olamaz.")
    @Size(min = 11, max = 11, message = "TCKN 11 haneli olmalıdır.")
    private String tckn;

    @NotBlank(message = "Ad alanı boş olamaz.")
    private String firstName;

    @NotBlank(message = "Soyad alanı boş olamaz.")
    private String lastName;

    @NotBlank(message = "Telefon numarası boş olamaz.")
    private String phone;

    @NotBlank(message = "E-posta alanı boş olamaz.")
    @Email(message = "Geçerli bir e-posta adresi giriniz.")
    private String email;

    @Valid
    @NotNull(message = "Müşteri bilgileri girilmelidir.")
    private UpdateCustomerRequest customer;
}
