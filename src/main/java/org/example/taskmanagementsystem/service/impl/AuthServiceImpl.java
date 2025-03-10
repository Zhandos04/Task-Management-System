package org.example.taskmanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.taskmanagementsystem.config.CustomAuthenticationProvider;
import org.example.taskmanagementsystem.dto.request.LoginDTO;
import org.example.taskmanagementsystem.dto.response.AuthDTO;
import org.example.taskmanagementsystem.entity.User;
import org.example.taskmanagementsystem.jwt.JwtService;
import org.example.taskmanagementsystem.service.AuthService;
import org.example.taskmanagementsystem.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserService userService;
    private final CustomAuthenticationProvider customAuthenticationProvider;
    private final JwtService jwtService;

    @Override
    public AuthDTO login(LoginDTO loginDTO) {
        Optional<User> userOptional = userService.getUserByEmail(loginDTO.getEmail());
        if (userOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неправильная почта или пароль!");
        }
        User user = userOptional.get();
        customAuthenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), loginDTO.getPassword())
        );
        if (!user.getIsVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Это пользователь еще не верифицирован!");
        }
        Map<String, String> tokens = jwtService.generateTokens(user.getEmail(), user.getRole().name());
        AuthDTO authDTO = new AuthDTO();
        authDTO.setAccessToken(tokens.get("accessToken"));
        authDTO.setRefreshToken(tokens.get("refreshToken"));
        authDTO.setRole(user.getRole().name());
        return authDTO;
    }

    @Override
    public AuthDTO refreshAccessToken(String refreshToken) {
        try {
            if (jwtService.validateRefreshToken(refreshToken)) {
                String userName = jwtService.extractUsername(refreshToken);
                User user = userService.getUserByEmail(userName)
                        .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден!"));
                UserDetails userDetails = userService.loadUserByUsername(userName);
                if (userDetails != null && !jwtService.isTokenExpired(refreshToken)) {
                    Map<String, String> newTokens = jwtService.generateTokens(userName, user.getRole().name());
                    AuthDTO authDTO = new AuthDTO();
                    authDTO.setAccessToken(newTokens.get("accessToken"));
                    authDTO.setRefreshToken(newTokens.get("refreshToken"));
                    authDTO.setRole(user.getRole().name());
                    return authDTO;
                }
            }
            throw new BadCredentialsException("Невалидный refresh токен");
        } catch (Exception e) {
            throw new BadCredentialsException("Невалидный refresh токен");
        }
    }
}
