package org.example.taskmanagementsystem.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.taskmanagementsystem.TaskManagementSystemApplication;
import org.example.taskmanagementsystem.dto.request.CodeDTO;
import org.example.taskmanagementsystem.dto.request.LoginDTO;
import org.example.taskmanagementsystem.dto.request.UserCreateDTO;
import org.example.taskmanagementsystem.dto.response.AuthDTO;
import org.example.taskmanagementsystem.entity.User;
import org.example.taskmanagementsystem.exceptions.UserAlreadyExistsException;
import org.example.taskmanagementsystem.service.AuthService;
import org.example.taskmanagementsystem.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TaskManagementSystemApplication.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    @MockBean
    private AuthService authService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // signup

    @Test
    @DisplayName("✅ Успешная регистрация пользователя")
    void registerUser_Success() throws Exception {
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setFirstName("Zhandos");
        userCreateDTO.setLastName("Nurbekuly");
        userCreateDTO.setEmail("220103393@stu.sdu.edu.kz");
        userCreateDTO.setPassword("SecurePass123!");

        doNothing().when(userService).registerNewUser(any(UserCreateDTO.class));

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDTO)))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Код успешно отправлен, Проверьте почту!"));

        verify(userService, times(1)).registerNewUser(any(UserCreateDTO.class));
    }

    @Test
    @DisplayName("❌ Ошибка валидации данных (400 Bad Request)")
    void registerUser_ValidationError() throws Exception {
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setFirstName("zhandos");
        userCreateDTO.setLastName("nurbekuly");
        userCreateDTO.setEmail("not-an-email");
        userCreateDTO.setPassword("12345");

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    @DisplayName("❌ Ошибка: Пользователь уже существует (406 Not Acceptable)")
    void registerUser_UserAlreadyExists() throws Exception {
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setFirstName("Zhandos");
        userCreateDTO.setLastName("Nurbekuly");
        userCreateDTO.setEmail("220103393@stu.sdu.edu.kz");
        userCreateDTO.setPassword("SecurePass123!");

        doThrow(new UserAlreadyExistsException("Пользователь с такой почтой уже существует!"))
                .when(userService).registerNewUser(any(UserCreateDTO.class));

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDTO)))
                .andExpect(status().isNotAcceptable())
                .andExpect(content().string("Пользователь с такой почтой уже существует!"));
    }

    @Test
    @DisplayName("❌ Ошибка: Проблема с отправкой email (500 Internal Server Error)")
    void registerUser_EmailSendingError() throws Exception {
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setFirstName("Zhandos");
        userCreateDTO.setLastName("Nurbekuly");
        userCreateDTO.setEmail("zhandos@example.com");
        userCreateDTO.setPassword("SecurePass123!");

        doThrow(new RuntimeException("Ошибка при отправлении кода в почту"))
                .when(userService).registerNewUser(any(UserCreateDTO.class));

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An unexpected error occurred: Ошибка при отправлении кода в почту"));
    }

    // verify-email

    @Test
    @DisplayName("✅ Успешная верификация email (201 Created)")
    void verifyEmail_Success() throws Exception {
        CodeDTO codeDTO = new CodeDTO();
        codeDTO.setEmail("zhandos@example.com");
        codeDTO.setCode("123456");

        User user = new User();
        user.setEmail("zhandos@example.com");
        user.setConfirmationCode("123456");
        user.setIsVerified(false);

        when(userService.getUserByEmail("zhandos@example.com")).thenReturn(Optional.of(user));
        doNothing().when(userService).verifyUser(any(User.class));

        mockMvc.perform(post("/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(codeDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Пользователь успешно зарегистрирован!"));

        verify(userService, times(1)).verifyUser(any(User.class));
    }

    @Test
    @DisplayName("❌ Ошибка: неправильный код (400 Bad Request)")
    void verifyEmail_WrongCode() throws Exception {
        CodeDTO codeDTO = new CodeDTO();
        codeDTO.setEmail("zhandos@example.com");
        codeDTO.setCode("wrong-code");

        User user = new User();
        user.setEmail("zhandos@example.com");
        user.setConfirmationCode("123456");

        when(userService.getUserByEmail("zhandos@example.com")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(codeDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Неправильный код"));
    }

    @Test
    @DisplayName("❌ Ошибка: пользователь не найден (404 Not Found)")
    void verifyEmail_UserNotFound() throws Exception {
        CodeDTO codeDTO = new CodeDTO();
        codeDTO.setEmail("notfound@example.com");
        codeDTO.setCode("123456");

        when(userService.getUserByEmail("notfound@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(codeDTO)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Пользователь не найден"));
    }

    // login

    @Test
    @DisplayName("✅ Успешный вход в систему")
    void login_Success() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("test@example.com");
        loginDTO.setPassword("SecurePass123!");

        AuthDTO authDTO = new AuthDTO();
        authDTO.setAccessToken("mockAccessToken");
        authDTO.setRefreshToken("mockRefreshToken");
        authDTO.setRole("ROLE_USER");

        when(authService.login(any(LoginDTO.class))).thenReturn(authDTO);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mockAccessToken"))
                .andExpect(jsonPath("$.refreshToken").value("mockRefreshToken"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));

        verify(authService, times(1)).login(any(LoginDTO.class));
    }

    @Test
    @DisplayName("❌ Ошибка: Неверный email или пароль (401 Unauthorized)")
    void login_InvalidCredentials() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("wrong@example.com");
        loginDTO.setPassword("WrongPassword");

        when(authService.login(any(LoginDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неправильная почта или пароль!"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("❌ Ошибка: Пользователь не верифицирован (403 Forbidden)")
    void login_UserNotVerified() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("unverified@example.com");
        loginDTO.setPassword("SecurePass123!");

        when(authService.login(any(LoginDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Это пользователь еще не верифицирован!"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isForbidden());
    }
}
