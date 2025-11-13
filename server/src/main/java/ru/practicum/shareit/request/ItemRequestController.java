package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

import static ru.practicum.shareit.Constant.OWNER_HEADER;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService service;

    // Создание нового запроса на предмет
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto create(@RequestHeader(OWNER_HEADER) Long userId,
                                 @RequestBody ItemRequestCreateDto itemRequestData) {
        log.info("POST /requests -> {} | {}", itemRequestData, userId);
        return service.create(itemRequestData, userId);
    }

    // Получение списка собственных запросов пользователя
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ItemRequestDto> getMyList(@RequestHeader(OWNER_HEADER) Long userId) {
        log.info("GET /requests | userid={}", userId);
        return service.getMyList(userId);
    }

    // Получение конкретного запроса по ID
    @GetMapping("/{requestId}")
    @ResponseStatus(HttpStatus.OK)
    public ItemRequestDto retrieve(@PathVariable long requestId) {
        log.info("GET /requests/{}", requestId);
        return service.retrieve(requestId);
    }

    // Получение общего списка всех запросов
    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public List<ItemRequestDto> getCommonList() {
        log.info("GET /requests/all");
        return service.getCommonList();
    }
}