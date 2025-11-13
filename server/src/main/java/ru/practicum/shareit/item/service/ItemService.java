package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    // Создание нового предмета
    ItemDto create(ItemCreateDto itemData, long userId);

    // Обновление существующего предмета
    ItemDto update(ItemDto itemData, long itemId, long userId);

    // Получение списка предметов пользователя
    List<ItemDto> getList(long userId);

    // Получение конкретного предмета по ID
    ItemDto retrieve(long itemId, long userId);

    // Поиск предметов по тексту
    List<ItemDto> search(String text);
}