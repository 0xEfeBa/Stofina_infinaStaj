package com.stofina.app.userservice.service.impl;

import com.stofina.app.userservice.exception.TokenRefreshException;
import com.stofina.app.userservice.exception.UserNotFoundException;
import com.stofina.app.userservice.mapper.UserMapper;
import com.stofina.app.userservice.model.User;
import com.stofina.app.userservice.repository.UserRepository;
import com.stofina.app.userservice.request.auth.LoginRequest;
import com.stofina.app.userservice.request.auth.TokenRefreshRequest;
import com.stofina.app.userservice.response.JwtResponse;
import com.stofina.app.userservice.response.TokenRefreshResponse;
import com.stofina.app.userservice.security.UserDetailsImpl;
import com.stofina.app.userservice.security.service.JwtTokenService;
import com.stofina.app.userservice.service.IAuthService;
import com.stofina.app.userservice.service.redis.RefreshTokenRedisService;
import com.stofina.app.userservice.util.UsernameGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.stofina.app.userservice.constants.UserConstants.*;
 

    @Service
    @Slf4j
    @RequiredArgsConstructor
    public class AuthServiceImpl implements IAuthService {

        private final AuthenticationManager authenticationManager;
        private final UserRepository userRepository;
        private final JwtTokenService jwtTokenService;
        private final RefreshTokenRedisService refreshTokenRedisService;
        private final UserMapper userMapper;
        @Override
        public ResponseEntity<?> Login(LoginRequest request) {
            try {
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(), request.getPassword())
                );

                log.info("Authentication success for user: {}", request.getEmail());

                SecurityContextHolder.getContext().setAuthentication(authentication);
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

                String accessToken = jwtTokenService.generateJwtToken(userDetails);
                String refreshToken = refreshTokenRedisService.createRefreshToken(userDetails.getId());

                User user = userRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new UserNotFoundException("User not found"));

                return ResponseEntity.ok(new JwtResponse(
                        accessToken,
                        refreshToken,
                        userMapper.toUserDto(user)
                ));

            } catch (DisabledException e) {
                log.error("Account disabled for user: {}", request.getEmail());
                throw new DisabledException(USER_ACCOUNT_DISABLED);
            } catch (BadCredentialsException e) {
                log.error("Invalid credentials for user: {}", request.getEmail());
                throw new BadCredentialsException(WRONG_USERNAME_OR_PASSWORD);
            } catch (UsernameNotFoundException e) {
                log.error("User not found: {}", request.getEmail());
                throw new UsernameNotFoundException(USER_NOT_FOUND);
            }
        }

        @Override
        public ResponseEntity<?> logoutUser(Long userId) {
            boolean deleted = refreshTokenRedisService.deleteRefreshTokenByUserId(userId);

            if (deleted) {
                return ResponseEntity.ok("Logout successful");
            } else {
                return ResponseEntity.status(404).body("No refresh token found for this user.");
            }

        }


        @Override
        public ResponseEntity<?> refreshToken(TokenRefreshRequest request) {
            String requestRefreshToken = request.getRefreshToken();

            return refreshTokenRedisService.getUserIdFromRefreshToken(requestRefreshToken)
                    .map(userId -> {
                        User user = userRepository.findById(userId)
                                .orElseThrow(() -> new UserNotFoundException("User not found"));

                        String newAccessToken = jwtTokenService.generateTokenByEmail(user.getEmail());

                        return ResponseEntity.ok(new TokenRefreshResponse(newAccessToken, requestRefreshToken));
                    })
                    .orElseThrow(() -> new TokenRefreshException(
                            requestRefreshToken, "Refresh token is not in Redis or expired!"));

        }



    }


