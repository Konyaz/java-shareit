package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserCreateRequestDto;
import ru.practicum.shareit.user.dto.UserRequestDto;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserClient client;

    // Создание нового пользователя
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(@Valid @RequestBody UserCreateRequestDto userData) {
        log.info("POST /users -> {}", userData);
        return client.create(userData);
    }

    // Обновление существующего пользователя
    @PatchMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> update(@PathVariable long userId,
                                         @Valid @RequestBody UserRequestDto newItemData) {
        log.info("PATCH /users/{} -> {}", userId, newItemData);
        return client.update(userId, newItemData);
    }

    // Получение списка всех пользователей
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> getList() {
        log.info("GET /users");
        return client.getList();
    }

    // Получение пользователя по ID
    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> retrieve(@PathVariable long userId) {
        log.info("GET /users/{}", userId);
        return client.retrieve(userId);
    }

    // Удаление пользователя
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Object> delete(@PathVariable long userId) {
        log.info("DELETE /users/{}", userId);
        return client.delete(userId);
    }
}