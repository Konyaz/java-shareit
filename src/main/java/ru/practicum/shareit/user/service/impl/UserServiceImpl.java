package ru.practicum.shareit.user.service.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DatabaseUniqueConstraintException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;
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

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto create(@Valid UserCreateDto userData) {
        if (userRepository.existsByEmail(userData.getEmail())) {
            log.error("Указанная почта уже зарегистрирована в приложении");
            throw new DatabaseUniqueConstraintException("Указанная почта уже зарегистрирована в приложении");
        }

        User newUser = UserMapper.toUser(userData);
        User savedUser = userRepository.save(newUser);
        log.info("Создан новый пользователь с ID: {}", savedUser.getId());
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto update(@Valid UserDto userData, long userId) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id={} не найден", userId);
                    return new NotFoundException(String.format("Пользователь с id=%s не найден", userId));
                });

        if (userData.getEmail() != null &&
                !userData.getEmail().equals(existingUser.getEmail()) &&
                userRepository.existsByEmail(userData.getEmail())) {
            log.error("Указанная почта уже зарегистрирована в приложении");
            throw new DatabaseUniqueConstraintException("Указанная почта уже зарегистрирована в приложении");
        }

        // Обновляем только не-null поля
        if (userData.getName() != null && !userData.getName().isBlank()) {
            existingUser.setName(userData.getName());
        }
        if (userData.getEmail() != null && !userData.getEmail().isBlank()) {
            existingUser.setEmail(userData.getEmail());
        }

        User updatedUser = userRepository.save(existingUser);
        log.info("Обновлен пользователь с ID: {}", userId);
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public List<UserDto> getList() {
        List<User> users = userRepository.findAll();
        log.debug("Получен список всех пользователей, количество: {}", users.size());
        return users.stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto retrieve(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id={} не найден", userId);
                    return new NotFoundException(String.format("Пользователь с id=%s не найден", userId));
                });
        log.debug("Получен пользователь с ID: {}", userId);
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public void delete(long userId) {
        if (!userRepository.existsById(userId)) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException(String.format("Пользователь с id=%s не найден", userId));
        }

        userRepository.deleteById(userId);
        log.info("Удален пользователь с ID: {}", userId);
    }
}