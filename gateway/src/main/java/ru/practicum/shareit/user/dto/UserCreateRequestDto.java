package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserCreateRequestDto {
    // Имя пользователя (обязательное поле)
    @NotBlank(message = "field is empty")
    private String name;

    // Email пользователя (обязательное поле, должен быть валидным email)
    @Email(message = "not valid email")
    @NotBlank(message = "field is empty")
    private String email;
}