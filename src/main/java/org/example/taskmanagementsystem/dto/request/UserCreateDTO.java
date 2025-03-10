package org.example.taskmanagementsystem.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateDTO {
    @Pattern(regexp = "^[A-Z][a-z]*$", message = "FirstName должно начинаться с заглавной буквы и содержать только маленькие буквы после первой")
    private String firstName;
    @Pattern(regexp = "^[A-Z][a-z]*$", message = "LastName должно начинаться с заглавной буквы и содержать только маленькие буквы после первой")
    private String lastName;
    @Email(message = "Неверный формат email")
    private String email;
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_#.])(?=.*[a-z])[A-Za-z\\d@$!%*?&_#.]{8,}$",
            message = "Password должен содержать как минимум одну заглавную букву, один чисел, и один символ. И должен быть длиной как минимум 8.")
    private String password;
}