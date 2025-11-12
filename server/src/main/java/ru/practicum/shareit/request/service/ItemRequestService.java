package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    // Создание нового запроса на предмет
    ItemRequestDto create(ItemRequestCreateDto requestData, long userId);

    // Получение списка собственных запросов пользователя
    List<ItemRequestDto> getMyList(long userId);

    // Получение общего списка всех запросов
    List<ItemRequestDto> getCommonList();

    // Получение конкретного запроса по ID
    ItemRequestDto retrieve(long requestId);
}