package ru.practicum.shareit.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemMapper;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestMapper;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final ItemRequestMapper itemRequestMapper;

    // Создание нового запроса на предмет
    @Override
    @Transactional
    public ItemRequestDto create(ItemRequestCreateDto requestData, long userId) {
        User author = userRepository.findById(userId).orElseThrow(() -> {
            log.error("Пользователь с id={} не найден", userId);
            return new NotFoundException(String.format("Пользователь с id=%s не найден", userId));
        });

        ItemRequest itemRequest = itemRequestMapper.toItemRequest(requestData);
        itemRequest.setAuthor(author);
        itemRequest.setCreated(LocalDateTime.now());

        return itemRequestMapper.toItemRequestDto(itemRequestRepository.save(itemRequest));
    }

    // Получение списка собственных запросов пользователя
    @Override
    public List<ItemRequestDto> getMyList(long userId) {
        if (!userRepository.existsById(userId)) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException(String.format("Пользователь с id=%s не найден", userId));
        }

        List<ItemRequest> itemRequests = itemRequestRepository.findByAuthorIdOrderByCreatedDesc(userId);

        // Получаем все ID запросов
        List<Long> requestIds = itemRequests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        // Загружаем все items для всех запросов одним запросом
        Map<Long, List<Item>> itemsByRequestId = itemRepository.findByRequestIdIn(requestIds).stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return itemRequests.stream()
                .map(itemRequest -> {
                    ItemRequestDto itemRequestDto = itemRequestMapper.toItemRequestDto(itemRequest);

                    // Добавляем список предметов из подготовленной мапы
                    List<Item> items = itemsByRequestId.getOrDefault(itemRequest.getId(), List.of());
                    itemRequestDto.setItems(items.stream()
                            .map(itemMapper::toItemDto)
                            .collect(Collectors.toList()));
                    return itemRequestDto;
                })
                .collect(Collectors.toList());
    }

    // Получение общего списка всех запросов
    @Override
    public List<ItemRequestDto> getCommonList() {
        List<ItemRequest> itemRequests = itemRequestRepository.findAll();

        // Получаем все ID запросов
        List<Long> requestIds = itemRequests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        // Загружаем все items для всех запросов одним запросом
        Map<Long, List<Item>> itemsByRequestId = itemRepository.findByRequestIdIn(requestIds).stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return itemRequests.stream()
                .map(itemRequest -> {
                    ItemRequestDto itemRequestDto = itemRequestMapper.toItemRequestDto(itemRequest);

                    // Добавляем список предметов из подготовленной мапы
                    List<Item> items = itemsByRequestId.getOrDefault(itemRequest.getId(), List.of());
                    itemRequestDto.setItems(items.stream()
                            .map(itemMapper::toItemDto)
                            .collect(Collectors.toList()));
                    return itemRequestDto;
                })
                .collect(Collectors.toList());
    }

    // Получение конкретного запроса по ID - ИСПРАВЛЕННЫЙ МЕТОД
    @Override
    public ItemRequestDto retrieve(long requestId) {
        ItemRequest itemRequest = itemRequestRepository.findById(requestId).orElseThrow(() -> {
            log.error("Запрос с id={} не найден", requestId);
            return new NotFoundException(String.format("Запрос с id=%s не найден", requestId));
        });
        ItemRequestDto itemRequestDto = itemRequestMapper.toItemRequestDto(itemRequest);

        // ИСПРАВЛЕНИЕ: Используем групповой запрос вместо отдельного
        // Создаем список из одного requestId и используем findByRequestIdIn
        List<Long> requestIds = List.of(requestId);
        Map<Long, List<Item>> itemsByRequestId = itemRepository.findByRequestIdIn(requestIds).stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        // Добавляем список предметов из подготовленной мапы
        List<Item> items = itemsByRequestId.getOrDefault(requestId, List.of());
        itemRequestDto.setItems(items.stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList()));

        return itemRequestDto;
    }
}