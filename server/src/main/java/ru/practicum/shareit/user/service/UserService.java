package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    // Создание нового пользователя
    UserDto create(UserCreateDto itemData);

    // Обновление существующего пользователя
    UserDto update(UserDto itemData, long userId);

    // Получение списка всех пользователей
    List<UserDto> getList();

    // Получение пользователя по ID
    UserDto retrieve(long userId);

    // Удаление пользователя
    void delete(long userId);
}