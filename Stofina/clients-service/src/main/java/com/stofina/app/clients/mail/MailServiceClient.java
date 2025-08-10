package com.stofina.app.clients.mail;


import com.stofina.app.commondata.model.EmailDetails;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "mail-service",
        fallback = MailServiceFallback.class
)
public interface MailServiceClient {

    @PostMapping("/api/v1/mails/forgot-password")
    ResponseEntity<Void> sendForgotPasswordEmail(@RequestBody EmailDetails emailDetails);

    @PostMapping("/api/v1/mails/create-password")
    ResponseEntity<Void> sendCreatePasswordEmail(@RequestBody EmailDetails emailDetails);
}
