package com.stofina.app.mailservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MailResource {
    private String attachmentFilename;
    private File file;
}