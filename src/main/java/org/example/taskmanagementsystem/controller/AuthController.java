package org.example.taskmanagementsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.taskmanagementsystem.dto.request.*;
import org.example.taskmanagementsystem.dto.response.AuthDTO;
import org.example.taskmanagementsystem.entity.User;
import org.example.taskmanagementsystem.exceptions.UserAlreadyExistsException;
import org.example.taskmanagementsystem.jwt.JwtService;
import org.example.taskmanagementsystem.service.EmailService;
import org.example.taskmanagementsystem.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@Tag(name="Auth", description="Взаймодействие с пользователями")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/signup")
    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Регистрирует пользователя, проверяя есть ли пользователь с такой почтой. Эндпоинт возвращает сообщения(статус)",
            responses = {
                    @ApiResponse(responseCode = "202", description = "Код успешно отправлен, Проверьте почту!"),
                    @ApiResponse(responseCode = "400", description = "Данные введены не правильно(Не прошел валидацию). В ошибке указывается все поля которые не прошли валидацию"),
                    @ApiResponse(responseCode = "406", description = "Пользователь с такой почтой уже существует!"),
                    @ApiResponse(responseCode = "500", description = "Ошибка при отправлении кода в почту")
            }
    )
    public ResponseEntity<String> register(@RequestBody @Valid UserCreateDTO userCreateDTO, BindingResult bindingResult) throws UserAlreadyExistsException {
        if (bindingResult.hasErrors()) {
            String errorMessages = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining("; "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessages);
        }
        userService.registerNewUser(userCreateDTO);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Код успешно отправлен, Проверьте почту!");
    }

    @PostMapping("/verify-email")
    @Operation(
            summary = "Верификация почты",
            description = "Проверяет код, введенный пользователем. Эндпоинт возвращает сообщения(статус)",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован!"),
                    @ApiResponse(responseCode = "400", description = "Неправильный код"),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
            }
    )
    public ResponseEntity<String> verifyEmail(@RequestBody CodeDTO codeDTO) {
        Optional<User> userOptional = userService.getUserByEmail(codeDTO.getEmail());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
        }
        User user = userOptional.get();
        if (!user.getConfirmationCode().equals(codeDTO.getCode())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Неправильный код");
        }
        userService.verifyUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("Пользователь успешно зарегистрирован!");
    }

    @PostMapping("/resend-code")
    @Operation(
            summary = "Повторно отправить код",
            description = "Повторно отправляет код подтверждения на электронную почту пользователя. Эндпоинт возвращает сообщения(статус)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Код успешно переотправлен!"),
                    @ApiResponse(responseCode = "404", description = "Пользователь с указанным адресом электронной почты не найден"),
                    @ApiResponse(responseCode = "500", description = "Ошибка при отправлении кода в почту")
            }
    )
    public ResponseEntity<String> resendCode(@RequestBody EmailDTO emailDTO) {
        userService.resentCode(emailDTO.getEmail());
        return ResponseEntity.ok("Код успешно переотправлен!");
    }

    @PostMapping("/login")
    @Operation(
            summary = "Вход пользователя",
            description = "Аутентифицирует пользователя и возвращает токены аутентификации и роль.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Вход выполнен успешно, токен доступа возвращен",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthDTO.class))),
                    @ApiResponse(responseCode = "401", description = "Неправильная почта или пароль!"),
                    @ApiResponse(responseCode = "403", description = "Это пользователь еще не верифицирован!")
            }
    )
    public ResponseEntity<AuthDTO> login(@RequestBody LoginDTO loginDTO) {
        AuthDTO authDTO = userService.login(loginDTO);
        return ResponseEntity.ok(authDTO);
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Выход пользователя",
            description = "Выходит пользователя, аннулируя текущий токен.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Выход выполнен успешно"),
                    @ApiResponse(responseCode = "400", description = "Невалидный токен")
            }
    )
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Невалидный токен!");
        }
        String token = authHeader.substring(7);
        userService.logout(token);
        return ResponseEntity.ok("Выход из системы успешно завершен!");
    }

    @PostMapping("/refresh-token")
    @Operation(
            summary = "Обновление токена доступа",
            description = "Обновляет токен доступа с использованием действительного refresh токена.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Токен доступа успешно обновлен",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthDTO.class))),
                    @ApiResponse(responseCode = "403", description = "Невалидный или истекший refresh токен"),
                    @ApiResponse(responseCode = "401", description = "Невалидный refresh токен")
            },
            security = @SecurityRequirement(name = "bearerToken")
    )
    public ResponseEntity<AuthDTO> refreshAccessToken(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth == null || !headerAuth.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String refreshToken = headerAuth.substring(7);
        AuthDTO authDTO = userService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(authDTO);
    }

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Восстановление пароля",
            description = "Инициирует процесс восстановления пароля, отправляя код сброса на электронную почту пользователя.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Код сброса успешно отправлен"),
                    @ApiResponse(responseCode = "404", description = "Электронная почта не найдена"),
                    @ApiResponse(responseCode = "403", description = "Пользователь еще не верифицирован"),
                    @ApiResponse(responseCode = "500", description = "Ошибка при отправлении кода в почту")
            }
    )
    public ResponseEntity<String> forgotPassword(@RequestBody EmailDTO emailDTO) {
        userService.forgotPassword(emailDTO.getEmail());
        return ResponseEntity.ok("Инструкции по восстановлению пароля были отправлены на вашу электронную почту.");
    }

    @PostMapping("/verify-code")
    @Operation(
            summary = "Верификация кода",
            description = "Верифицирует код сброса, введённый пользователем.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Код верифицирован!"),
                    @ApiResponse(responseCode = "400", description = "Неправильный код"),
                    @ApiResponse(responseCode = "404", description = "Почта не найдена")
            }
    )
    public ResponseEntity<String> verifyCode(@RequestBody CodeDTO codeDTO) {
        userService.verifyResetCode(codeDTO.getEmail(), codeDTO.getCode());
        return ResponseEntity.ok("Код верифицирован!");
    }

    @PostMapping("/update-password")
    @Operation(
            summary = "Обновление пароля пользователя",
            description = "Обновляет пароль пользователя после верификации.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Пароль успешно обновлен"),
                    @ApiResponse(responseCode = "400", description = "Неверный ввод (ошибки валидации)"),
                    @ApiResponse(responseCode = "404", description = "Почта не найдена")
            }
    )
    public ResponseEntity<String> updatePassword(
            @RequestBody @Valid UpdatePasswordDTO updatePasswordDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessages = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining("; "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessages);
        }
        userService.updatePassword(updatePasswordDTO);
        return ResponseEntity.ok("Пароль обновлен!");
    }
}