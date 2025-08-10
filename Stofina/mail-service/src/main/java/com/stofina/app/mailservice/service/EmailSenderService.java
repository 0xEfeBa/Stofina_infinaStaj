package com.stofina.app.mailservice.service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.context.Context;
import com.stofina.app.mailservice.model.Email;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;

@Service
public class EmailSenderService {

    private final JavaMailSender emailSender;
    private final SpringTemplateEngine templateEngine;

    public EmailSenderService(JavaMailSender emailSender, SpringTemplateEngine templateEngine) {
        this.emailSender = emailSender;
        this.templateEngine = templateEngine;
    }

    public void sendEmail(Email mail) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
        if (!CollectionUtils.isEmpty(mail.getAttachments())) {
            mail.getAttachments()
                    .forEach(mailResource ->
                            {

                                try {
                                    helper.addAttachment(
                                            mailResource.getAttachmentFilename(),
                                            mailResource.getFile()
                                    );
                                } catch (MessagingException e) {
                                    throw new RuntimeException(e);
                                }

                            }
                    );
        }
        Context context = new Context();
        context.setVariables(mail.getParameters());

        String html = templateEngine.process(mail.getTemplate(), context);
        helper.setTo(mail.getTo().toArray(new String[0]));
        helper.setText(html, true);
        helper.setSubject(mail.getSubject());
        helper.setFrom(mail.getFrom());
        emailSender.send(message);
    }
}
