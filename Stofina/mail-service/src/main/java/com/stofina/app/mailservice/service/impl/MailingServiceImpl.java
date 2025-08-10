package com.stofina.app.mailservice.service.impl;

import com.stofina.app.commondata.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import com.stofina.app.mailservice.exception.EmailSendingException;
import com.stofina.app.mailservice.model.EmailDetails;
import com.stofina.app.mailservice.service.EmailSenderService;
import com.stofina.app.mailservice.service.MailingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.stofina.app.mailservice.config.EmailConfiguration;
import com.stofina.app.mailservice.model.Email;

import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Service
@Slf4j
public class MailingServiceImpl implements MailingService {
    private static final String TEMPLATE_FORGOT_PASSWORD = "forgotPassword";
    private static final String TEMPLATE_CREATE_PASSWORD = "createPassword";
    private static final String TEMPLATE_NEW_USER_CREATED = "newUserCreated";
    private static final String SEND_EMAIL_FIRST_NAME = "firstName";
    private static final String SEND_EMAIL_LAST_NAME = "lastName";

    @Value("${app.frontend-url}")
    private String url;

    private final EmailSenderService emailSender;

    @Override
    public void sendForgotPasswordEmail(EmailDetails emailDetails) {
        final Email email = buildEmail(emailDetails, TEMPLATE_FORGOT_PASSWORD, getForgotPasswordEmailSubject());
        try {
            sendEmail(email);
        } catch (Exception e) {
            log.error("Error sending forgot password email: ", e.getMessage());
            throw new EmailSendingException("Failed to send forgot password email.");
        }
    }

    @Override
    public void sendCreatePasswordEmail(EmailDetails emailDetails) {
        final Email email = buildEmail(emailDetails, TEMPLATE_CREATE_PASSWORD, getCreatePasswordEmailSubject());
        try {
            sendEmail(email);
        } catch (Exception e) {
            log.error("Error sending create password email: ", e.getMessage());
            throw new EmailSendingException("Failed to send create password email.");
        }
    }

    @Override
    @Async(value = EmailConfiguration.MAIL_SEND_EXECUTOR)
    public void sendEmail(String emailTemplate, Map<String, Object> params, String subject, Set<String> to) {
        final Email email = Email.EmailBuilder.anEmail()
                .withFrom(getFromMailAddress())
                .withTo(to)
                .withSubject(subject)
                .withTemplate(emailTemplate)
                .withParameters(params)
                .build();
        try {
            emailSender.sendEmail(email);
        } catch (Exception e) {
            log.error("Error sending email with template: {} - {}", emailTemplate, e.getMessage());
            throw new EmailSendingException("Failed to send email with template: " + emailTemplate);
        }
    }

    @Override
    public void sendUserAccountCreatedEmail(UserDto user) {
        final EmailDetails emailDetails = new EmailDetails(user.getEmail(), user.getFirstName(), user.getLastName(), getApplicationBaseUrl());
        final Email email = buildEmail(emailDetails, TEMPLATE_NEW_USER_CREATED, getCreatePasswordEmailSubject());
        try {
            sendEmail(email);
        } catch (Exception e) {
            log.error("Error sending user account created email: ", e.getMessage());
            throw new EmailSendingException("Failed to send user account created email.");
        }
    }

    private Email buildEmail(EmailDetails emailDetails, String template, String subject) {
        return Email.EmailBuilder.anEmail()
                .withFrom(getFromMailAddress())
                .withTo(emailDetails.getEmail())
                .withSubject(subject)
                .withTemplate(template)
                .withParameter(SEND_EMAIL_FIRST_NAME, emailDetails.getUserName())
                .withParameter(SEND_EMAIL_LAST_NAME, emailDetails.getLastName())
                .withParameter("url", emailDetails.getLink())
                .build();
    }

    @SneakyThrows
    private void sendEmail(Email email) {
        emailSender.sendEmail(email);
    }

    private String getFromMailAddress() {
        return "cdilsiz33.13@gmail.com";
    }

    private String getForgotPasswordEmailSubject() {
        return "Forgot Your Password ?";
    }

    private String getCreatePasswordEmailSubject() {
        return "Create New Password";
    }

    private String getApplicationBaseUrl() {
        return url;
    }
}
