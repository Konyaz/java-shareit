package ru.practicum.shareit.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DatabaseUniqueConstraintException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dao.UserMapper;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserMapper mapper;

    // Создание нового пользователя
    @Override
    @Transactional
    public UserDto create(UserCreateDto userData) {
        User newUser = mapper.toUser(userData);

        // Проверка уникальности email
        if (repository.existsByEmail(newUser.getEmail())) {
            log.error("Указанная почта уже зарегистрирована в приложении");
            throw new DatabaseUniqueConstraintException("Указанная почта уже зарегистрирована в приложении");
        }

        return mapper.toUserDto(repository.save(newUser));
    }

    // Обновление существующего пользователя
    @Override
    @Transactional
    public UserDto update(UserDto userData, long userId) {
        User existedUser = repository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id={} не найден", userId);
                    return new NotFoundException(String.format("Пользователь с id=%s не найден", userId));
                });

        // Проверка уникальности нового email
        if (userData.getEmail() != null &&
                !userData.getEmail().equals(existedUser.getEmail()) &&
                repository.existsByEmail(userData.getEmail())) {
            log.error("Указанная почта уже зарегистрирована в приложении");
            throw new DatabaseUniqueConstraintException("Указанная почта уже зарегистрирована в приложении");
        }

        // Обновление имени если оно указано и не пустое
        if (userData.getName() != null && !userData.getName().isBlank()) {
            existedUser.setName(userData.getName());
        }
        // Обновление email если он указан и не пустой
        if (userData.getEmail() != null && !userData.getEmail().isBlank()) {
            existedUser.setEmail(userData.getEmail());
        }

        return mapper.toUserDto(existedUser);
    }

    // Получение списка всех пользователей
    @Override
    public List<UserDto> getList() {
        return repository.findAll().stream()
                .map(mapper::toUserDto)
                .collect(Collectors.toList());
    }

    // Получение пользователя по ID
    @Override
    public UserDto retrieve(long userId) {
        return mapper.toUserDto(repository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id={} не найден", userId);
                    return new NotFoundException(String.format("Пользователь с id=%s не найден", userId));
                }));
    }

    // Удаление пользователя
    @Override
    @Transactional
    public void delete(long userId) {
        if (!repository.existsById(userId)) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException(String.format("Пользователь с id=%s не найден", userId));
        }

        repository.deleteById(userId);
    }
}