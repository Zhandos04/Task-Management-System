package org.example.taskmanagementsystem.service;

import org.example.taskmanagementsystem.dto.request.LoginDTO;
import org.example.taskmanagementsystem.dto.response.AuthDTO;

public interface AuthService {
    AuthDTO login(LoginDTO loginDTO);
    AuthDTO refreshAccessToken(String refreshToken);
}
