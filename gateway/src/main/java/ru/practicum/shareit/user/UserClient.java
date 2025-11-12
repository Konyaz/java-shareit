package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserCreateRequestDto;
import ru.practicum.shareit.user.dto.UserRequestDto;

@Service
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    // Создание нового пользователя
    public ResponseEntity<Object> create(@Valid UserCreateRequestDto requestDto) {
        return post("", requestDto);
    }

    // Обновление существующего пользователя
    public ResponseEntity<Object> update(@PathVariable long userId, @Valid UserRequestDto requestDto) {
        return patch("/" + userId, requestDto);
    }

    // Получение списка всех пользователей
    public ResponseEntity<Object> getList() {
        return get("");
    }

    // Получение пользователя по ID
    public ResponseEntity<Object> retrieve(@PathVariable long userId) {
        return get("/" + userId);
    }

    // Удаление пользователя
    public ResponseEntity<Object> delete(@PathVariable long userId) {
        return delete("/" + userId);
    }
}