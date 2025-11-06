package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService service;
    public static final String OWNER_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ItemDto create(@RequestHeader(OWNER_HEADER) @NotNull Long userId,
                          @Valid @RequestBody ItemCreateDto itemData) {
        log.info("POST /items -> {} | userid={}", itemData, userId);
        return service.create(itemData, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(OWNER_HEADER) @NotNull Long userId,
                          @PathVariable long itemId,
                          @Valid @RequestBody ItemUpdateDto newItemData) {
        log.info("PATCH /items/{} -> {} | userid={}", itemId, newItemData, userId);
        return service.update(newItemData, itemId, userId);
    }

    @GetMapping
    public List<ItemWithBookingsDto> getList(@RequestHeader(OWNER_HEADER) @NotNull Long userId) {
        log.info("GET /items | userid={}", userId);
        return service.getList(userId);
    }

    @GetMapping("/{itemId}")
    public ItemDetailedDto retrieve(@RequestHeader(OWNER_HEADER) @NotNull Long userId,
                                    @PathVariable long itemId) {
        log.info("GET /items/{} | userid={}", itemId, userId);
        return service.retrieve(itemId, userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        log.info("GET /items/search?text={}", text);
        return service.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(OWNER_HEADER) @NotNull Long userId,
                                 @PathVariable long itemId,
                                 @Valid @RequestBody CommentCreateDto commentCreateDto) {
        log.info("POST /items/{}/comment -> {} | userid={}", itemId, commentCreateDto, userId);
        return service.addComment(itemId, userId, commentCreateDto);
    }
}