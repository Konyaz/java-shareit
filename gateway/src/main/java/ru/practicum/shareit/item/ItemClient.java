package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentCreateRequestDto;
import ru.practicum.shareit.item.dto.ItemCreateRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    // Создание нового предмета
    public ResponseEntity<Object> create(long userId, ItemCreateRequestDto requestDto) {
        return post("", userId, requestDto);
    }

    // Обновление существующего предмета
    public ResponseEntity<Object> update(long itemId, ItemRequestDto requestDto) {
        return patch("/" + itemId, requestDto);
    }

    // Получение списка предметов пользователя
    public ResponseEntity<Object> getList(long userId) {
        return get("", userId);
    }

    // Получение конкретного предмета по ID
    public ResponseEntity<Object> retrieve(long itemId, long userId) {
        return get("/" + itemId, userId);
    }

    // Поиск предметов по тексту
    public ResponseEntity<Object> search(String text) {
        return get("/search?text=" + text);
    }

    // Создание комментария к предмету
    public ResponseEntity<Object> createComment(long userId, long itemId, CommentCreateRequestDto commentCreateDto) {
        return post("/" + itemId + "/comment", userId, commentCreateDto);
    }
}