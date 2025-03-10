package org.example.taskmanagementsystem.service;

import org.example.taskmanagementsystem.dto.request.LoginDTO;
import org.example.taskmanagementsystem.dto.request.UpdatePasswordDTO;
import org.example.taskmanagementsystem.dto.request.UserCreateDTO;
import org.example.taskmanagementsystem.dto.response.AuthDTO;
import org.example.taskmanagementsystem.entity.User;
import org.example.taskmanagementsystem.exceptions.UserAlreadyExistsException;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public interface UserService {
    UserDetails loadUserByUsername(String username);
    void registerNewUser(UserCreateDTO userCreateDTO) throws UserAlreadyExistsException;
    void verifyUser(User user);
    void updatePassword(UpdatePasswordDTO updatePasswordDTO);
    Optional<User> getUserByEmail(String email);
    UserDetails getCurrentUser();
    void removeExpiredUnverifiedUsers();
    void resentCode(String email);
    void logout(String token);
    void forgotPassword(String email);
    void verifyResetCode(String email, String code);
}
