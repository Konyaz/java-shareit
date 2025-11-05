package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemDao {
    Item create(Item itemData);

    Item update(Item itemData);

    List<Item> getList(long userId);

    Item getById(long itemId);

    List<Item> searchAvailableItems(String text);

    Boolean exists(long itemId);

    Boolean isOwnership(long itemId, long userId);
}