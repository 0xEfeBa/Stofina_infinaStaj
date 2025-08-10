package com.stofina.app.mailservice.controller;

import com.stofina.app.commondata.dto.UserDto;
import lombok.RequiredArgsConstructor;
import com.stofina.app.mailservice.model.EmailDetails;
import com.stofina.app.mailservice.service.MailingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import static com.stofina.app.mailservice.constant.EmailConstant.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(API_PREFIX+API_VERSION_V1+API_MAIL)
@Tag(name = "Email Controller", description = "Controller for handling email related operations")
public class EmailController {
    private final MailingService mailingService;


    @PostMapping("/forgot-password")
    @Operation(summary = "Send Forgot Password Email", description = "Sends an email to reset the password", responses = {
            @ApiResponse(responseCode = "200", description = "Email sent successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content)})
    public ResponseEntity<Void> sendForgotPasswordEmail(@RequestBody EmailDetails emailDetails) {
        mailingService.sendForgotPasswordEmail(emailDetails);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/create-password")
    @Operation(summary = "Send Create Password Email", description = "Sends an email to create a new password", responses = {
            @ApiResponse(responseCode = "200", description = "Email sent successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content)})
    public ResponseEntity<Void> sendCreatePasswordEmail(@RequestBody EmailDetails emailDetails) {
        mailingService.sendCreatePasswordEmail(emailDetails);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/account-created")
    @Operation(summary = "Send User Account Created Email", description = "Sends an email to notify that the user account has been created", responses = {
            @ApiResponse(responseCode = "200", description = "Email sent successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content)})
    public ResponseEntity<Void> sendUserAccountCreatedEmail(@RequestBody UserDto userDto) {
        mailingService.sendUserAccountCreatedEmail(userDto);
        return ResponseEntity.ok().build();
    }
}
