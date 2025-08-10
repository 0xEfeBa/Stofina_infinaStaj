package com.stofina.app.commondata.dto;

import com.stofina.app.commondata.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;

    private String firstName;

    private String lastName;

    private String username;

    private String title;

    private String phoneNumber;

    private String email;

    private UserStatus status;

    private Set<RoleDto> roles;
}