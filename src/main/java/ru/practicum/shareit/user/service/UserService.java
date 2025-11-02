package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto create(UserCreateDto itemData);

    UserDto update(UserDto itemData, long userId);

    List<UserDto> getList();

    UserDto retrieve(long userId);

    void delete(long userId);
}