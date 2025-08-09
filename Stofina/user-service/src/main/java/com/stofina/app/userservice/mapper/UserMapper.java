package com.stofina.app.userservice.mapper;

import com.stofina.app.commondata.dto.UserDto;
import com.stofina.app.userservice.model.User;
import com.stofina.app.userservice.request.user.CreateUserRequest;
import com.stofina.app.userservice.request.user.UpdateUserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import java.util.List;

@Mapper(componentModel = "spring", uses = { RoleMapper.class })
public interface UserMapper {

    UserDto toUserDto(User user);

    List<UserDto> toUserDtoList(List<User> userList);

    User createUser(CreateUserRequest request);
    void updateUser(@MappingTarget User user, UpdateUserRequest request);

}
