package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @PostMapping
    public UserDto create(@Valid @RequestBody UserCreateDto userData) {
        log.info("POST /users -> {}", userData);
        return service.create(userData);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable long userId,
                          @Valid @RequestBody UserDto newItemData) {
        log.info("PATCH /users/{} -> {}", userId, newItemData);
        return service.update(newItemData, userId);
    }

    @GetMapping
    public List<UserDto> getList() {
        log.info("GET /users");
        return service.getList();
    }

    @GetMapping("/{userId}")
    public UserDto retrieve(@PathVariable long userId) {
        log.info("GET /users/{}", userId);
        return service.retrieve(userId);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable long userId) {
        log.info("DELETE /users/{}", userId);
        service.delete(userId);
    }
}