package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.*;

import java.util.List;

public interface ItemService {
    ItemDto create(ItemCreateDto itemData, long userId);

    ItemDto update(ItemUpdateDto itemData, long itemId, long userId);

    List<ItemWithBookingsDto> getList(long userId);

    ItemDetailedDto retrieve(long itemId, long userId);

    List<ItemDto> search(String text);

    CommentDto addComment(long itemId, long userId, CommentCreateDto commentCreateDto);
}