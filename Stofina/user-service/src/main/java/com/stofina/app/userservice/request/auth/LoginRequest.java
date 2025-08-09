package com.stofina.app.userservice.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message ="Email Cannot Be Null")
    @Email
    private String email;

    @NotBlank(message ="Password Cannot Be Null")
    private String password;
}
