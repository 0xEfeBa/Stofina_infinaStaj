package com.stofina.app.mailservice.service;

 import com.stofina.app.mailservice.model.EmailDetails;
 import com.stofina.app.commondata.dto.UserDto;
 import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public interface MailingService {

    /**
     * Sends a "forgot password" email to the user.
     *
     * This method constructs and sends an email to the user containing a link to reset their password.
     * The email is customized with the user's first and last name for a personalized touch.
     *
     * @param emailDetails Contains the details required to send the email, including the recipient's email address,
     *                     user's first name, last name, and the link for password reset.
     */
    void sendForgotPasswordEmail(EmailDetails emailDetails);

    /**
     * Sends an account creation or "set password" email to the user.
     *
     * Similar to the forgot password email, this method sends an email to the user with a link to set or create a new password
     * for their account. The email includes personalization with the user's first and last name.
     *
     * @param emailDetails Contains the details required to send the email, including the recipient's email address,
     *                     user's first name, last name, and the link for setting the password.
     */
    void sendCreatePasswordEmail(EmailDetails emailDetails);

    /**
     * Sends an email based on a specified template and parameters.
     *
     * This method allows sending emails that are more generic and can be customized via templates and parameters.
     * It's used for sending various types of emails, not limited to user-related notifications.
     *
     * @param emailTemplate The name of the email template to use.
     * @param params A map of parameters that will be used to fill in the template.
     * @param subject The subject of the email.
     * @param to A set of recipient email addresses.
     */
    void sendEmail(String emailTemplate, Map<String, Object> params, String subject, Set<String> to);

    /**
     * Notifies a user that their account has been created.
     *
     * This method sends an email to the user indicating that their account has been successfully created.
     * The email can include details such as the user's name and instructions for next steps, such as logging in or setting a password.
     *
     * @param user A UserDto object containing details of the user whose account has been created.
     */
    void sendUserAccountCreatedEmail(UserDto user);
}