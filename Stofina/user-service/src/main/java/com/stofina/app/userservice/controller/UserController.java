package com.stofina.app.userservice.controller;

import com.stofina.app.commondata.dto.UserDto;
import com.stofina.app.userservice.model.User;
import com.stofina.app.userservice.request.user.*;
import com.stofina.app.userservice.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.http.Header;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.stofina.app.userservice.constants.UserConstants.*;


@Tag(
        name = "CRUD REST APIs for User in Stofina",
        description = "CRUD REST APIs in Stofina to CREATE, UPDATE, FETCH AND DELETE account details"
)
@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
@Slf4j
@RequestMapping(value = API_PREFIX + API_VERSION_V1 + API_USER)
public class UserController {

    private final IUserService userService;

    @Operation(
            summary = "Create User REST API",
            description = "REST API to create new User inside Stofina"
    )
    @ApiResponses(
            @ApiResponse(
                    responseCode = "201",
                    description = "HTTP Status CREATED",
                    content = @Content(
                            schema = @Schema(implementation = UserDto.class),
                            mediaType = "application/json")))
    @PostMapping("/create-user")
    @ResponseStatus(HttpStatus.CREATED)
    @CrossOrigin("http://localhost:3000")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest user) {
        return userService.saveUser(user);
    }


    @Operation(
            summary = "Update User Details REST API",
            description = "REST API to update User details based on cargo id"
    )
    @ApiResponses(
            @ApiResponse(
                    responseCode = "200",
                    description = "HTTP Status OK",
                    content = @Content(
                            schema = @Schema(implementation = UserDto.class),
                            mediaType = "application/json")))
    @PutMapping("/update/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @CrossOrigin("http://localhost:3000")
    public UserDto updateUser(@Valid @RequestBody UpdateUserRequest request, @PathVariable ("userId") Long userId){
        return userService.updateUser(request, userId);
    }

    @Operation(
            summary = "Fetch User Details REST API",
            description = "REST API to fetch User details based on a cargo id"
    )
    @ApiResponses(
            @ApiResponse(
                    responseCode = "200",
                    description = "HTTP Status OK",
                    content = @Content(
                            schema = @Schema(implementation = UserDto.class),
                            mediaType = "application/json")))
    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @CrossOrigin("http://localhost:3000")
    public UserDto getByUserId(@PathVariable("userId")  Long userId){
        return userService.getUserById( userId);
    }

    @Operation(
            summary = "Change User Password REST API",
            description = "REST API toChange User Password based on User Email"
    )
    @ApiResponses(
            @ApiResponse(
                    responseCode = "200",
                    description = "HTTP Status OK",
                    content = @Content(
                            schema = @Schema(implementation = ResponseEntity.class),
                            mediaType = "application/json")))
    @CrossOrigin("http://localhost:3000")
    @PutMapping("/create-password")
    public ResponseEntity createPassword(@Valid @RequestBody CreatePasswordRequest request ) {
        userService.setUserPassword(request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Change User Password REST API",
            description = "REST API toChange User Password based on User Email"
    )
    @ApiResponses(
            @ApiResponse(
                    responseCode = "200",
                    description = "HTTP Status OK",
                    content = @Content(
                            schema = @Schema(implementation = ResponseEntity.class),
                            mediaType = "application/json")))
    @CrossOrigin("http://localhost:3000")
    @PutMapping("/change-password")
    public ResponseEntity changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Send User Forgot Password Link REST API",
            description = "REST API to Send User Forgot Password Link "
    )
    @ApiResponses(
            @ApiResponse(
                    responseCode = "200",
                    description = "HTTP Status OK",
                    content = @Content(
                            schema = @Schema(implementation = ResponseEntity.class),
                            mediaType = "application/json")))
    @GetMapping("/forgot-password")
    @ResponseStatus(HttpStatus.OK)
    @CrossOrigin("http://localhost:3000")
    public void sendForgetPasswordLink(@RequestParam String email) {
        userService.sendForgotPasswordMail(email);
    }
    @Operation(
            summary = "Send User Reset Password REST API",
            description = "REST API to Reset User Password  "
    )
    @ApiResponses(
            @ApiResponse(
                    responseCode = "200",
                    description = "HTTP Status OK",
                    content = @Content(
                            schema = @Schema(implementation = ResponseEntity.class),
                            mediaType = "application/json")))
    @CrossOrigin("http://localhost:3000")
    @PostMapping("/reset-password")
    public ResponseEntity resetForgottenPassword(@Valid @RequestBody ResetPasswordRequest request) {
        boolean reset = userService.resetUserPassword(request);
        return reset ? ResponseEntity.ok("Password has been reset successfully.")
                : ResponseEntity.badRequest().body("Invalid or expired token.");
    }
    @Operation(
            summary = "Get All Users REST API",
            description = "REST API to retrieve all users in the system"
    )
    @ApiResponses(
            @ApiResponse(
                    responseCode = "200",
                    description = "HTTP Status OK",
                    content = @Content(
                            schema = @Schema(implementation = UserDto.class),
                            mediaType = "application/json"
                    )
            )
    )
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @CrossOrigin("http://localhost:3000")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    }

