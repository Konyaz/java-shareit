package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentCreateRequestDto;
import ru.practicum.shareit.item.dto.ItemCreateRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;

import static ru.practicum.shareit.Constant.OWNER_HEADER;

@Slf4j
@RestController
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemClient client;

    // Создание нового предмета
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(@RequestHeader(OWNER_HEADER) @NotNull @Positive Long userId,
                                         @Valid @RequestBody ItemCreateRequestDto itemData) {
        log.info("POST /items -> {} | userid={}", itemData, userId);
        return client.create(userId, itemData);
    }

    // Обновление существующего предмета
    @PatchMapping("/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> update(@RequestHeader(OWNER_HEADER) @NotNull @Positive Long userId,
                                         @PathVariable @Positive long itemId,
                                         @Valid @RequestBody ItemRequestDto newItemData) {
        log.info("PATCH /items/{} -> {} | userid={}", itemId, newItemData, userId);
        return client.update(itemId, newItemData);
    }

    // Получение списка предметов пользователя
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> getList(@RequestHeader(OWNER_HEADER) @NotNull @Positive Long userId) {
        log.info("GET /items | userid={}", userId);
        return client.getList(userId);
    }

    // Получение конкретного предмета по ID
    @GetMapping("/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> retrieve(@RequestHeader(OWNER_HEADER) @NotNull @Positive Long userId,
                                           @PathVariable @Positive long itemId) {
        log.info("GET /items/{} | userid={}", itemId, userId);
        return client.retrieve(itemId, userId);
    }

    // Поиск предметов по тексту
    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> search(@RequestParam String text) {
        log.info("GET /items/search?text={}", text);
        return client.search(text);
    }

    // Создание комментария к предмету
    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> createComment(@RequestHeader(OWNER_HEADER) @NotNull @Positive Long userId,
                                                @PathVariable @Positive long itemId,
                                                @Valid @RequestBody CommentCreateRequestDto commentCreateDto) {
        log.info("POST /items/{}/comment -> {} | userid={}", itemId, commentCreateDto, userId);
        return client.createComment(userId, itemId, commentCreateDto);
    }
}