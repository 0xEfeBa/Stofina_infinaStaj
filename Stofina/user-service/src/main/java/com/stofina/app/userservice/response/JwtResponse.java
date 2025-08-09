package com.stofina.app.userservice.response;

import com.stofina.app.commondata.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JwtResponse {
    private String accessToken;
    @Builder.Default
    private String type = "Bearer";
    private String refreshToken;
    private UserDto userDto;

    public JwtResponse(String accessToken, String refreshToken , UserDto userDto) {
        this.accessToken = accessToken;
        this.userDto=userDto;
        this.refreshToken=refreshToken;
    }
}