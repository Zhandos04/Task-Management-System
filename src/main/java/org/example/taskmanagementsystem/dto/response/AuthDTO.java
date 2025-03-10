package org.example.taskmanagementsystem.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthDTO {
    private String accessToken;
    private String refreshToken;
    private String role;
}