package ru.practicum.shareit.user.dto;

import lombok.Data;

@Data
public class UserCreateDto {
    // ID пользователя
    private Long id;
    // Имя пользователя
    private String name;
    // Email пользователя
    private String email;
}