package com.stofina.app.userservice.service.impl;

import com.stofina.app.clients.mail.MailServiceClient;
import com.stofina.app.commondata.dto.UserDto;
import com.stofina.app.commondata.model.EmailDetails;
import com.stofina.app.commondata.model.enums.UserStatus;
import com.stofina.app.userservice.exception.EmailAlreadyExistsException;
import com.stofina.app.userservice.exception.PasswordMistMatchException;
import com.stofina.app.userservice.exception.UserNotFoundException;
import com.stofina.app.userservice.mapper.UserMapper;
import com.stofina.app.userservice.model.Role;
import com.stofina.app.userservice.model.User;
import com.stofina.app.userservice.repository.RoleRepository;
import com.stofina.app.userservice.repository.UserRepository;
import com.stofina.app.userservice.request.user.ChangePasswordRequest;
import com.stofina.app.userservice.request.user.CreatePasswordRequest;
import com.stofina.app.userservice.request.user.CreateUserRequest;
import com.stofina.app.userservice.request.user.ResetPasswordRequest;
import com.stofina.app.userservice.request.user.UpdateUserRequest;
import com.stofina.app.userservice.service.IUserService;
import com.stofina.app.userservice.security.UserDetailsImpl;
import com.stofina.app.userservice.service.redis.RedisTokenService;
import com.stofina.app.userservice.util.AppSettingsKey;
import com.stofina.app.userservice.util.UsernameGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

import static com.stofina.app.userservice.constants.UserConstants.ROLE_NOT_FOUND;
import static com.stofina.app.userservice.constants.UserConstants.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService, UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailServiceClient mailServiceClient;
    private final RedisTokenService redisTokenService;
    private final UserMapper userMapper;
    private final UsernameGenerator usernameGenerator;


    @Value("${app.url}")
    private String url;

    @Override
    @Transactional
    public UserDto updateUser(UpdateUserRequest request, Long userId) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already in use: " + request.getEmail());
        }
        User user = findUserById(userId);
        userMapper.updateUser(user, request);
        if (request.getRoleTypes() != null && !request.getRoleTypes().isEmpty()) {
            Set<Role> roles = request.getRoleTypes().stream()
                    .map(roleType -> roleRepository.findByRoleType(roleType)
                            .orElseThrow(() -> new RuntimeException(ROLE_NOT_FOUND)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        return userMapper.toUserDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public ResponseEntity<UserDto> saveUser(CreateUserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already in use: " + request.getEmail());
        }
        User user = userMapper.createUser(request);
        Set<Role> roles = request.getRoleTypes().stream()
                .map(roleType -> roleRepository.findByRoleType(roleType)
                        .orElseThrow(() -> new RuntimeException(ROLE_NOT_FOUND)))
                .collect(Collectors.toSet());
        user.setRoles(roles);

        String activationToken = redisTokenService.generateToken();
        redisTokenService.saveToken("activation", activationToken, user.getEmail());
        user.setStatus(UserStatus.INACTIVE);
        user.setUsername(usernameGenerator.generateUsername(request.getFirstName(), request.getLastName()));
        userRepository.save(user);
        String link = String.format(AppSettingsKey.ACTIVATE_PASSWORD_URL_FORMAT, url, activationToken);
        EmailDetails emailDetails = EmailDetails.builder()
                .link(link)
                .userName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();
        mailServiceClient.sendCreatePasswordEmail(emailDetails);
        return ResponseEntity.ok(userMapper.toUserDto(user));
    }

    @Override
    @Transactional
    public void setUserPassword(CreatePasswordRequest request) {
        String email = redisTokenService.getEmail("activation", request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Token not found or expired."));

        User user = findUserByEmail(email);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        redisTokenService.deleteToken("activation", request.getToken());
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = findUserByEmail(request.getEmail());

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new PasswordMistMatchException("Current password is incorrect.");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void sendForgotPasswordMail(String email) {
        User user = findUserByEmail(email);
        String resetToken = redisTokenService.generateToken();
        redisTokenService.saveToken("reset", resetToken, user.getEmail());

        String link = String.format(AppSettingsKey.RESET_PASSWORD_URL_FORMAT, url, resetToken);

        EmailDetails emailDetails = EmailDetails.builder()
                .link(link)
                .userName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();
        mailServiceClient.sendForgotPasswordEmail(emailDetails);
    }

    @Override
    @Transactional
    public boolean resetUserPassword(ResetPasswordRequest request) {
        String email = redisTokenService.getEmail("reset", request.getToken())
                .orElse(null);

        if (email == null) return false;

        User user = findUserByEmail(email);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        redisTokenService.deleteToken("reset", request.getToken());
        return true;
    }

    @Override
    public UserDto getUserById(Long id) {
        return userMapper.toUserDto(findUserById(id));
    }

    @Override
    public UserDetails loadUserByUsername(String email)  {
        return UserDetailsImpl.build(userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND)));
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
    }
    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

}
