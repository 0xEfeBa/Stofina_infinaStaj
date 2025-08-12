package com.stofina.app.userservice.service;

import com.stofina.app.userservice.exception.TokenRefreshException;
import com.stofina.app.userservice.mapper.UserMapper;
import com.stofina.app.userservice.model.User;
import com.stofina.app.userservice.repository.UserRepository;
import com.stofina.app.userservice.request.auth.LoginRequest;
import com.stofina.app.userservice.request.auth.TokenRefreshRequest;
import com.stofina.app.userservice.response.JwtResponse;
import com.stofina.app.userservice.response.TokenRefreshResponse;
import com.stofina.app.userservice.security.UserDetailsImpl;
import com.stofina.app.userservice.security.service.JwtTokenService;
import com.stofina.app.userservice.service.impl.AuthServiceImpl;
import com.stofina.app.userservice.service.redis.RefreshTokenRedisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock UserRepository userRepository;
    @Mock JwtTokenService jwtTokenService;
    @Mock RefreshTokenRedisService refreshTokenRedisService;
    @Mock UserMapper userMapper;

    @InjectMocks AuthServiceImpl authService;

    @Test
    @DisplayName("Login: başarı → JwtResponse")
    void login_success() {
        LoginRequest req = LoginRequest.builder().email("a@b.com").password("pw").build();
        Authentication auth = mock(Authentication.class);
        UserDetailsImpl principal =
                new UserDetailsImpl(10L, "a@b.com", "ENC", List.of());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(principal);
        when(jwtTokenService.generateJwtToken(principal)).thenReturn("ACCESS");
        when(refreshTokenRedisService.createRefreshToken(10L)).thenReturn("REFRESH");

        User user = new User(); user.setEmail("a@b.com");
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(userMapper.toUserDto(user)).thenReturn(com.stofina.app.commondata.dto.UserDto.builder().id(10L).email("a@b.com").build());

        ResponseEntity<?> resp = authService.Login(req);
        assertEquals(200, resp.getStatusCode().value());
        assertTrue(resp.getBody() instanceof JwtResponse);
    }

    @Test
    @DisplayName("Login: hesap disabled → DisabledException")
    void login_disabled_throws() {
        LoginRequest req = LoginRequest.builder().email("d@d.com").password("x").build();
        when(authenticationManager.authenticate(any())).thenThrow(new DisabledException("disabled"));
        assertThrows(DisabledException.class, () -> authService.Login(req));
    }

    @Test
    @DisplayName("Login: kötü şifre → BadCredentialsException")
    void login_badCredentials_throws() {
        LoginRequest req = LoginRequest.builder().email("a@b.com").password("bad").build();
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));
        assertThrows(BadCredentialsException.class, () -> authService.Login(req));
    }

    @Test
    @DisplayName("Login: kullanıcı yok → UsernameNotFoundException")
    void login_usernameNotFound_throws() {
        LoginRequest req = LoginRequest.builder().email("no@no.com").password("x").build();
        when(authenticationManager.authenticate(any())).thenThrow(new UsernameNotFoundException("nope"));
        assertThrows(UsernameNotFoundException.class, () -> authService.Login(req));
    }

    @Test
    @DisplayName("refreshToken: geçerli → yeni access")
    void refresh_success() {
        TokenRefreshRequest req = new TokenRefreshRequest("REFRESH");
        when(refreshTokenRedisService.getUserIdFromRefreshToken("REFRESH")).thenReturn(Optional.of(10L));
        User user = new User(); user.setId(10L); user.setEmail("a@b.com");
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(jwtTokenService.generateTokenByEmail("a@b.com")).thenReturn("NEW_ACCESS");

        ResponseEntity<?> resp = authService.refreshToken(req);
        assertEquals(200, resp.getStatusCode().value());
        TokenRefreshResponse body = (TokenRefreshResponse) resp.getBody();
        assertEquals("NEW_ACCESS", body.getAccessToken());
    }

    @Test
    @DisplayName("refreshToken: Redis'te yok/expired → TokenRefreshException")
    void refresh_missing_throws() {
        TokenRefreshRequest req = new TokenRefreshRequest("BAD");
        when(refreshTokenRedisService.getUserIdFromRefreshToken("BAD")).thenReturn(Optional.empty());
        assertThrows(TokenRefreshException.class, () -> authService.refreshToken(req));
    }
}
