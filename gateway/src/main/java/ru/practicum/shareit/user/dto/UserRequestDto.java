package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserRequestDto {
    // ID пользователя
    private Long id;
    // Имя пользователя
    private String name;
    // Email пользователя (должен быть валидным email)
    @Email(message = "Please provide a valid email address.")
    private String email;
}