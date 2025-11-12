package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    // Создание нового пользователя
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody UserCreateDto userData) {
        log.info("POST /users -> {}", userData);
        return service.create(userData);
    }

    // Обновление существующего пользователя
    @PatchMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto update(@PathVariable long userId,
                          @RequestBody UserDto newItemData) {
        log.info("PATCH /users/{} -> {}", userId, newItemData);
        return service.update(newItemData, userId);
    }

    // Получение списка всех пользователей
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getList() {
        log.info("GET /users");
        return service.getList();
    }

    // Получение пользователя по ID
    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto retrieve(@PathVariable long userId) {
        log.info("GET /users/{}", userId);
        return service.retrieve(userId);
    }

    // Удаление пользователя
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long userId) {
        log.info("DELETE /users/{}", userId);
        service.delete(userId);
    }
}