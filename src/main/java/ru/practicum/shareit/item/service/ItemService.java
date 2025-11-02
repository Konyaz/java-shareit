package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto create(ItemCreateDto itemData, long userId);

    ItemDto update(ItemDto itemData, long itemId, long userId);

    List<ItemDto> getList(long userId);

    ItemDto retrieve(long itemId, long userId);

    List<ItemDto> search(String text);
}