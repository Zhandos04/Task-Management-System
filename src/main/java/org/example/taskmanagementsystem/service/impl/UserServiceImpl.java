package org.example.taskmanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.taskmanagementsystem.config.CustomAuthenticationProvider;
import org.example.taskmanagementsystem.dto.request.LoginDTO;
import org.example.taskmanagementsystem.dto.request.UpdatePasswordDTO;
import org.example.taskmanagementsystem.dto.request.UserCreateDTO;
import org.example.taskmanagementsystem.dto.response.AuthDTO;
import org.example.taskmanagementsystem.entity.Role;
import org.example.taskmanagementsystem.entity.User;
import org.example.taskmanagementsystem.exceptions.UserAlreadyExistsException;
import org.example.taskmanagementsystem.jwt.JwtService;
import org.example.taskmanagementsystem.repository.UserRepository;
import org.example.taskmanagementsystem.service.TokenBlacklistService;
import org.example.taskmanagementsystem.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final EmailServiceImpl emailService;
    private final CustomAuthenticationProvider authenticationProvider;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    @Transactional
    public void registerNewUser(UserCreateDTO userCreateDTO) throws UserAlreadyExistsException {
        if (userRepository.existsByEmail(userCreateDTO.getEmail())) {
            throw new UserAlreadyExistsException("Пользователь с такой почтой уже существует!");
        }
        User user = modelMapper.map(userCreateDTO, User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setIsVerified(false);
        user.setRole(Role.ROLE_USER);
        String code = generateCode();
        user.setConfirmationCode(code);
        user.setCodeSentAt(LocalDateTime.now());
        userRepository.save(user);
        try {
            emailService.sendEmail(user.getEmail(), "Task Management System Verify Email", "Your code is: " + code);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при отправлении кода в почту");
        }
    }

    @Override
    @Transactional
    public void verifyUser(User user){
        user.setIsVerified(true);
        user.setCreatedAt(Instant.now());
        userRepository.save(user);
    }
    @Override
    @Transactional
    public void resentCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с указанным адресом электронной почты не найден"));
        String code = generateCode();
        user.setConfirmationCode(code);
        userRepository.save(user);
        try {
            emailService.sendEmail(user.getEmail(), "Task Management System Verify Email", "Your code is: " + code);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при отправлении кода в почту");
        }
    }

    @Override
    public AuthDTO login(LoginDTO loginDTO) {
        Optional<User> userOptional = getUserByEmail(loginDTO.getEmail());
        if (userOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неправильная почта или пароль!");
        }
        User user = userOptional.get();
        authenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), loginDTO.getPassword())
        );
        if (!user.getIsVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Это пользователь еще не верифицирован!");
        }
        Map<String, String> tokens = jwtService.generateTokens(loginDTO.getEmail(), user.getRole().name());

        AuthDTO authDTO = new AuthDTO();
        authDTO.setAccessToken(tokens.get("accessToken"));
        authDTO.setRefreshToken(tokens.get("refreshToken"));
        authDTO.setRole(user.getRole().name());
        return authDTO;
    }

    @Override
    public void logout(String token) {
        Date expirationTime = jwtService.extractExpiration(token);
        tokenBlacklistService.addTokenToBlacklist(token, expirationTime);
    }

    @Override
    public AuthDTO refreshAccessToken(String refreshToken) {
        try {
            if (jwtService.validateRefreshToken(refreshToken)) {
                String userName = jwtService.extractUsername(refreshToken);
                User user = userRepository.findByEmail(userName)
                        .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден!"));
                UserDetails userDetails = loadUserByUsername(userName);
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

    @Override
    @Transactional
    public void forgotPassword(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Почта не найдена!");
        }
        User user = optionalUser.get();
        if (!user.getIsVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Пользователь еще не верифицирован!");
        }

        String code = generateCode();
        user.setConfirmationCode(code);
        userRepository.save(user);
        try {
            emailService.sendEmail(user.getEmail(), "Task Management System Verify Email", "Your code is: " + code);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при отправлении кода в почту");
        }
    }

    @Override
    public void verifyResetCode(String email, String code) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Почта не найдена!");
        }
        User user = optionalUser.get();
        if (!user.getConfirmationCode().equals(code)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неправильный код!");
        }
    }

    @Override
    @Transactional
    public void updatePassword(UpdatePasswordDTO updatePasswordDTO) {
        Optional<User> optionalUser = userRepository.findByEmail(updatePasswordDTO.getEmail());
        if (optionalUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Почта не найдена!");
        }
        User user = optionalUser.get();
        user.setPassword(passwordEncoder.encode(updatePasswordDTO.getPassword()));
        userRepository.save(user);
    }

    @Override
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void removeExpiredUnverifiedUsers() {
        LocalDateTime expirationTime = LocalDateTime.now().minusHours(24);
        userRepository.deleteExpiredUnverifiedUsers(expirationTime);
    }


    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    private String generateCode() {
        return Integer.toString((int)(Math.random() * 900000) + 100000);
    }

    @Override
    public UserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            return (UserDetails) authentication.getPrincipal();
        }

        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = getUserByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), user.getAuthorities());
    }
}
