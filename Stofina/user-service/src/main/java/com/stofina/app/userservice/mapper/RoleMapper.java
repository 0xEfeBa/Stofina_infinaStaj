package com.stofina.app.userservice.mapper;

import com.stofina.app.commondata.dto.RoleDto;
import com.stofina.app.userservice.model.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(source = "roleType", target = "roleType")
    RoleDto toRoleDto(Role role);
}
