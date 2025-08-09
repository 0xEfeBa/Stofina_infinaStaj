package com.stofina.app.userservice.request.user;


import com.stofina.app.commondata.model.enums.RoleType;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserRequest {

    @NotBlank(message = "Ad alanı boş bırakılamaz.")
    private String firstName;

    @NotBlank(message = "Soyad alanı boş bırakılamaz.")
    private String lastName;


    @Size(max = 50, message = "Unvan en fazla 50 karakter olabilir.")
    private String title;

    @Column(nullable = false)
    @Pattern(regexp = "^[1-9][0-9]{9}$", message = "Geçerli bir telefon numarası giriniz. Örn: 5321234567")
    private String phoneNumber;

    @NotBlank(message = "E-posta adresi boş bırakılamaz.")
    @Email(message = "Geçerli bir e-posta adresi giriniz.")
    private String email;

    @NotNull(message = "Rol bilgisi boş bırakılamaz.")
    @Size(min = 1, message = "En az bir rol seçilmelidir.")
    private Set<RoleType> roleTypes;

}
