package com.stofina.app.userservice.service;

import com.stofina.app.clients.mail.MailServiceClient;
import com.stofina.app.commondata.dto.UserDto;
import com.stofina.app.commondata.model.EmailDetails;
import com.stofina.app.commondata.model.enums.UserStatus;
import com.stofina.app.userservice.exception.EmailAlreadyExistsException;
import com.stofina.app.userservice.exception.PasswordMistMatchException;
import com.stofina.app.userservice.exception.UserNotFoundException;
import com.stofina.app.userservice.mapper.UserMapper;
import com.stofina.app.userservice.model.User;
import com.stofina.app.userservice.repository.RoleRepository;
import com.stofina.app.userservice.repository.UserRepository;
import com.stofina.app.userservice.request.user.ChangePasswordRequest;
import com.stofina.app.userservice.request.user.CreatePasswordRequest;
import com.stofina.app.userservice.request.user.CreateUserRequest;
import com.stofina.app.userservice.request.user.UpdateUserRequest;
import com.stofina.app.userservice.service.impl.UserServiceImpl;
import com.stofina.app.userservice.service.redis.RedisTokenService;
import com.stofina.app.userservice.util.UsernameGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.stofina.app.userservice.constants.UserConstants.ROLE_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock MailServiceClient mailServiceClient;
    @Mock RedisTokenService redisTokenService;
    @Mock UserMapper userMapper;
    @Mock UsernameGenerator usernameGenerator;

    @InjectMocks UserServiceImpl userService;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(userService, "url", "http://localhost:3000");

        lenient().when(passwordEncoder.encode(anyString()))
                .thenAnswer(inv -> "ENC(" + inv.getArgument(0) + ")");

        lenient().when(passwordEncoder.matches(anyString(), anyString()))
                .thenAnswer(inv -> {
                    String raw = inv.getArgument(0);
                    String enc = inv.getArgument(1);
                    return Objects.equals(enc, "ENC(" + raw + ")");
                });

        lenient().when(usernameGenerator.generateUsername(anyString(), anyString()))
                .thenAnswer(inv -> (inv.getArgument(0) + "." + inv.getArgument(1)).toLowerCase());

        lenient().when(redisTokenService.generateToken()).thenReturn("TEST_TOKEN");
    }


    @Test
    @DisplayName("saveUser: email zaten kayıtlı → EmailAlreadyExistsException")
    void saveUser_emailExists_throws() {
        CreateUserRequest req = new CreateUserRequest();
        req.setEmail("a@b.com");
        when(userRepository.existsByEmail("a@b.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.saveUser(req));
        verify(userRepository).existsByEmail("a@b.com");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("saveUser: rol bulunamadı → RuntimeException(ROLE_NOT_FOUND)")
    void saveUser_roleNotFound_throws() {
        CreateUserRequest req = new CreateUserRequest();
        req.setEmail("x@y.com");
        req.setFirstName("Ali");
        req.setLastName("Veli");
        var roleType = com.stofina.app.commondata.model.enums.RoleType.CUSTOMER_TRADER;
        req.setRoleTypes(Set.of(roleType));

        when(userRepository.existsByEmail("x@y.com")).thenReturn(false);
        when(userMapper.createUser(req)).thenReturn(new User());
        when(roleRepository.findByRoleType(roleType)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.saveUser(req));
        assertEquals(ROLE_NOT_FOUND, ex.getMessage());
    }

    @Test
    @DisplayName("saveUser: başarı → token üret, e-mail gönder, DTO dön")
    void saveUser_success() {
        CreateUserRequest req = new CreateUserRequest();
        req.setEmail("x@y.com");
        req.setFirstName("Ali");
        req.setLastName("Veli");
        req.setRoleTypes(Set.of()); // basit tutalım

        User entity = new User();
        entity.setId(10L);
        entity.setEmail("x@y.com");

        when(userRepository.existsByEmail("x@y.com")).thenReturn(false);
        when(userMapper.createUser(req)).thenReturn(entity);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        when(userMapper.toUserDto(any(User.class))).thenReturn(
                UserDto.builder().id(10L).username("ali.veli").email("x@y.com").build()
        );

        when(redisTokenService.generateToken()).thenReturn("T0K3N");

        ResponseEntity<UserDto> resp = userService.saveUser(req);
        assertEquals(200, resp.getStatusCode().value());
        assertEquals(10L, resp.getBody().getId());
        assertEquals("ali.veli", resp.getBody().getUsername());

        verify(redisTokenService).saveToken(eq("activation"), eq("T0K3N"), eq("x@y.com"));
        verify(mailServiceClient).sendCreatePasswordEmail(any(EmailDetails.class));
    }

    @Test
    @DisplayName("updateUser: e-mail çakışması → EmailAlreadyExistsException")
    void updateUser_emailExists_throws() {
        UpdateUserRequest req = new UpdateUserRequest();
        req.setEmail("new@x.com");
        when(userRepository.existsByEmail("new@x.com")).thenReturn(true);
        assertThrows(EmailAlreadyExistsException.class, () -> userService.updateUser(req, 5L));
    }



    @Test
    @DisplayName("getUserById: yok → UserNotFoundException")
    void getById_notFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(99L));
    }

    @Test
    @DisplayName("setUserPassword: token yok/expired → IllegalArgumentException")
    void setUserPassword_tokenMissing_throws() {
        CreatePasswordRequest req = new CreatePasswordRequest();
        req.setToken("BAD");
        req.setNewPassword("New123!");

        when(redisTokenService.getEmail("activation", "BAD")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.setUserPassword(req));
    }

    @Test
    @DisplayName("setUserPassword: başarı → encode, ACTIVE, token sil")
    void setUserPassword_success() {
        CreatePasswordRequest req = new CreatePasswordRequest();
        req.setToken("OK");
        req.setNewPassword("New123!");

        when(redisTokenService.getEmail("activation", "OK")).thenReturn(Optional.of("a@b.com"));
        User user = new User(); user.setEmail("a@b.com");
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("New123!")).thenReturn("ENC");

        userService.setUserPassword(req);

        assertEquals("ENC", user.getPassword());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        verify(redisTokenService).deleteToken("activation","OK");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("changePassword: current yanlış → PasswordMistMatchException")
    void changePassword_wrongCurrent() {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setEmail("a@b.com");
        req.setCurrentPassword("old");
        req.setNewPassword("new");

        User user = new User(); user.setEmail("a@b.com"); user.setPassword("ENC_OLD");
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old","ENC_OLD")).thenReturn(false);

        assertThrows(PasswordMistMatchException.class, () -> userService.changePassword(req));
    }

    @Test
    @DisplayName("changePassword: başarı")
    void changePassword_success() {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setEmail("a@b.com");
        req.setCurrentPassword("old");
        req.setNewPassword("new");

        User user = new User(); user.setEmail("a@b.com"); user.setPassword("ENC_OLD");
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old","ENC_OLD")).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("ENC_NEW");

        userService.changePassword(req);
        assertEquals("ENC_NEW", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("sendForgotPasswordMail: başarı → reset token kaydet + mail gönder")
    void sendForgotPasswordMail_success() {
        User user = new User(); user.setEmail("a@b.com"); user.setFirstName("Ali"); user.setLastName("Veli");
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(redisTokenService.generateToken()).thenReturn("RST123");

        userService.sendForgotPasswordMail("a@b.com");

        verify(redisTokenService).saveToken(eq("reset"), eq("RST123"), eq("a@b.com"));
        verify(mailServiceClient).sendForgotPasswordEmail(any(EmailDetails.class));
    }

    @Test
    @DisplayName("resetUserPassword: token geçersiz → false")
    void resetUserPassword_invalidToken_false() {
        when(redisTokenService.getEmail("reset","BAD")).thenReturn(Optional.empty());
        var ok = userService.resetUserPassword(
                new com.stofina.app.userservice.request.user.ResetPasswordRequest("BAD","New!")
        );
        assertFalse(ok);
    }

    @Test
    @DisplayName("resetUserPassword: başarı → yeni şifre set + token sil → true")
    void resetUserPassword_success_true() {
        when(redisTokenService.getEmail("reset","OK")).thenReturn(Optional.of("a@b.com"));
        User user = new User(); user.setEmail("a@b.com");
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("N")).thenReturn("ENC");

        var ok = userService.resetUserPassword(
                new com.stofina.app.userservice.request.user.ResetPasswordRequest("OK","N")
        );
        assertTrue(ok);
        assertEquals("ENC", user.getPassword());
        verify(redisTokenService).deleteToken("reset","OK");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("getAllUsers: map to DTO list")
    void getAllUsers_maps() {
        User u = new User(); u.setId(1L);
        when(userRepository.findAll()).thenReturn(List.of(u));
        when(userMapper.toUserDto(any(User.class))).thenReturn(UserDto.builder().id(1L).build());

        var list = userService.getAllUsers();
        assertEquals(1, list.size());
        assertEquals(1L, list.get(0).getId());
    }
}
