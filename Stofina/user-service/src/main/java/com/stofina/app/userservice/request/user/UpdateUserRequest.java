package com.stofina.app.userservice.request.user;

import com.stofina.app.commondata.model.enums.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateUserRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String nationalId;

    @Email
    @NotBlank
    private String email;

    @NotNull
    private Set<RoleType> roleTypes;

    private Long reportsToId;
}

