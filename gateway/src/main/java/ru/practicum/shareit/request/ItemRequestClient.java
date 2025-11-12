package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestCreateRequestDto;

@Service
public class ItemRequestClient extends BaseClient {
    private static final String API_PREFIX = "/requests";

    @Autowired
    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    // Создание нового запроса на предмет
    public ResponseEntity<Object> create(long userId, ItemRequestCreateRequestDto requestDto) {
        return post("", userId, requestDto);
    }

    // Получение списка собственных запросов пользователя
    public ResponseEntity<Object> getMyList(long userId) {
        return get("", userId);
    }

    // Получение общего списка всех запросов
    public ResponseEntity<Object> getCommonList() {
        return get("/all");
    }

    // Получение конкретного запроса по ID
    public ResponseEntity<Object> retrieve(long requestId) {
        return get("/" + requestId);
    }
}