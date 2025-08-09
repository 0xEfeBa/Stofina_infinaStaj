package com.stofina.app.userservice.service;

import com.stofina.app.userservice.request.auth.LoginRequest;
import com.stofina.app.userservice.request.auth.TokenRefreshRequest;
import org.springframework.http.ResponseEntity;


public interface IAuthService {

    /**
     * Authenticates a user using the provided login credentials.
     *
     * @param request the login request containing username/email and password
     * @return a {@link ResponseEntity} containing access and refresh tokens if authentication is successful,
     *         or an error response otherwise
     */
    ResponseEntity<?> Login(LoginRequest request);

    /**
     * Logs out a user by invalidating their session and tokens.
     *
     * @param userID the ID of the user to log out
     * @return a {@link ResponseEntity} indicating the result of the logout operation
     */
    ResponseEntity<?> logoutUser(Long userID);

    /**
     * Refreshes the access token using a valid refresh token.
     *
     * @param request the token refresh request containing the refresh token
     * @return a {@link ResponseEntity} containing a new access token,
     *         or an error response if the refresh token is invalid or expired
     */
    ResponseEntity<?> refreshToken(TokenRefreshRequest request);
}
