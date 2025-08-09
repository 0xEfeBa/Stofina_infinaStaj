package com.stofina.app.userservice.controller;


import com.stofina.app.userservice.request.auth.LoginRequest;
import com.stofina.app.userservice.request.auth.TokenRefreshRequest;
import com.stofina.app.userservice.response.JwtResponse;
import com.stofina.app.userservice.security.service.JwtTokenService;
import com.stofina.app.userservice.service.IAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.stofina.app.userservice.constants.UserConstants.*;


@RestController
@RequestMapping(API_PREFIX + API_VERSION_V1+API_AUTHENTICATION)
@RequiredArgsConstructor
@Tag(
        name = "CRUD REST APIs for Auth in Stofina",
        description = "CRUD REST APIs in Stofina to CREATE, UPDATE, FETCH AND DELETE account details"
)
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {
    private final IAuthService service;
    private final JwtTokenService jwtTokenService;

    @Operation(
            summary = "Login User ",
            description = "REST API to Login "
    )
    @ApiResponses(
            @ApiResponse(
                    responseCode = "200",
                    description = "HTTP Status OK",
                    content = @Content(
                            schema = @Schema(implementation = JwtResponse.class),
                            mediaType = "application/json")))
    @PostMapping("/login")
    @CrossOrigin("http://localhost:3000")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        return service.Login(loginRequest);
    }
    @Operation(
            summary = "Validate token",
            description = "Rest Api To Validate User Token")
    @ApiResponses(value =
    @ApiResponse(
            responseCode = "201",
            description = "Validate token",
            content = @Content(
                    mediaType = "application/json")))
    @ResponseStatus(HttpStatus.ACCEPTED)
    @GetMapping("/validate")
    @CrossOrigin("http://localhost:3000")
    public boolean validateToken(@RequestParam("token") String token) {
        return   jwtTokenService.validateToken(token);

    }
    @Operation(
            summary = "Refresh Token Request",
            description = "REST API to get Refresh Token "
    )
    @ApiResponses(
            @ApiResponse(
                    responseCode = "200",
                    description = "HTTP Status OK",
                    content = @Content(
                            schema = @Schema(implementation = TokenRefreshRequest.class),
                            mediaType = "application/json")))
    @PostMapping("/refreshtoken")
    @ResponseStatus(HttpStatus.OK)
    @CrossOrigin("http://localhost:3000")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        return service.refreshToken(request);
    }
    @Operation(
            summary = "Logout User Request",
            description = "REST API to get logout user Token "
    )
    @ApiResponses(
            @ApiResponse(
                    responseCode = "200",
                    description = "HTTP Status OK",
                    content = @Content(
                             mediaType = "application/json")))
    @PostMapping("/logout/{userId}")
    @CrossOrigin("http://localhost:3000")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> logoutUser(@PathVariable("userId") Long userId) {
        return service.logoutUser(userId);
    }

}
