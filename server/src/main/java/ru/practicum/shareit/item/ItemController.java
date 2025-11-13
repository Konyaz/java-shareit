package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.CommentService;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static ru.practicum.shareit.Constant.OWNER_HEADER;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService service;
    private final CommentService commentService;

    // Создание нового предмета
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(@RequestHeader(OWNER_HEADER) Long userId,
                          @RequestBody ItemCreateDto itemData) {
        log.info("POST /items -> {} | userid={}", itemData, userId);
        return service.create(itemData, userId);
    }

    // Обновление существующего предмета
    @PatchMapping("/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    public ItemDto update(@RequestHeader(OWNER_HEADER) Long userId,
                          @PathVariable long itemId,
                          @RequestBody ItemDto newItemData) {
        log.info("PATCH /items/{} -> {} | userid={}", itemId, newItemData, userId);
        return service.update(newItemData, itemId, userId);
    }

    // Получение списка предметов пользователя
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ItemDto> getList(@RequestHeader(OWNER_HEADER) Long userId) {
        log.info("GET /items | userid={}", userId);
        return service.getList(userId);
    }

    // Получение конкретного предмета по ID
    @GetMapping("/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    public ItemDto retrieve(@RequestHeader(OWNER_HEADER) Long userId,
                            @PathVariable long itemId) {
        log.info("GET /items/{} | userid={}", itemId, userId);
        return service.retrieve(itemId, userId);
    }

    // Поиск предметов по тексту
    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<ItemDto> search(@RequestParam String text) {
        log.info("GET /items/search?text={}", text);
        return service.search(text);
    }

    // Создание комментария к предмету
    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto createComment(@RequestHeader(OWNER_HEADER) Long userId,
                                    @PathVariable long itemId,
                                    @RequestBody CommentCreateDto commentCreateDto) {
        log.info("POST /items/{}/comment -> {} | userid={}", itemId, commentCreateDto, userId);
        return commentService.create(userId, itemId, commentCreateDto);
    }
}