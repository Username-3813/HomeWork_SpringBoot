package com.garage.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistrationRequest {
    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
    private String password;

    private String lastName;

    @NotBlank(message = "Имя обязательно")
    private String firstName;

    private String patronymic;
    private String role = "USER";

    @Past(message = "Дата рождения не может быть в будущем")
    private LocalDate birthDate;

    private String phone;
}