package com.stofina.app.clients.mail;


import com.stofina.app.commondata.model.EmailDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MailServiceFallback implements MailServiceClient {

    @Override
    public ResponseEntity<Void> sendForgotPasswordEmail(EmailDetails emailDetails) {
        log.warn("MailService DOWN — sendForgotPasswordEmail fallback triggered for email: {}", emailDetails.getEmail());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> sendCreatePasswordEmail(EmailDetails emailDetails) {
        log.warn("MailService DOWN — sendCreatePasswordEmail fallback triggered for email: {}", emailDetails.getEmail());
        return ResponseEntity.ok().build();
    }
}
