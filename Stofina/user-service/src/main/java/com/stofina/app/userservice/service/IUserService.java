package com.stofina.app.userservice.service;

import com.stofina.app.commondata.dto.UserDto;
import com.stofina.app.userservice.request.user.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IUserService {

    /**
     * Updates an existing user's information.
     *
     * @param request the updated user details
     * @param userId  the ID of the user to update
     * @return updated {@link UserDto}
     */
    UserDto updateUser(UpdateUserRequest request, Long userId);

    /**
     * Changes the password of the currently authenticated user.
     *
     * @param changePasswordRequest contains old and new password details
     */
    void changePassword(ChangePasswordRequest changePasswordRequest);

    /**
     * Creates a new user based on the provided request.
     *
     * @param request the user creation request
     * @return a {@link ResponseEntity} indicating the result of the operation
     */
    ResponseEntity<?> saveUser(CreateUserRequest request);

    /**
     * Sends a password reset email to the user with the specified email address.
     *
     * @param email the user's email address
     */
    void sendForgotPasswordMail(String email);

    /**
     * Resets the user's password using the provided token and new password.
     *
     * @param request the reset password request
     * @return true if the password was successfully reset, false otherwise
     */
    boolean resetUserPassword(ResetPasswordRequest request);

    /**
     * Retrieves a user's details by their ID.
     *
     * @param id the ID of the user
     * @return {@link UserDto} representing the user
     */
    UserDto getUserById(Long id);

    /**
     * Sets the initial password for a user (typically after activation or onboarding).
     *
     * @param request contains the new password and token
     */
    void setUserPassword(CreatePasswordRequest request);



    /**
     * Retrieves a list of all users in the system.
     *
     * @return list of {@link UserDto}
     */
    List<UserDto> getAllUsers();

}
