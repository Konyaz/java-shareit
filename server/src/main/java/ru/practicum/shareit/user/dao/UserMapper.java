package ru.practicum.shareit.user.dao;

import org.mapstruct.Mapper;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    // Маппинг объекта User в UserDto
    UserDto toUserDto(User user);

    // Маппинг объекта UserDto в User
    User toUser(UserDto userDto);

    // Маппинг объекта UserCreateDto в User
    User toUser(UserCreateDto userDto);
}