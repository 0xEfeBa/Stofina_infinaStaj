package com.stofina.app.mailservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailDetails {
    private String email;
    private String userName;
    private String lastName;
    private String link;
}
