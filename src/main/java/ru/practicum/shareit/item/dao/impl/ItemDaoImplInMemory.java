package ru.practicum.shareit.item.dao.impl;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class ItemDaoImplInMemory implements ItemDao {
    private final Map<Long, Item> db = new HashMap<>();
    private long currentId = 1L;

    private long getNextId() {
        return currentId++;
    }

    @Override
    public Item create(Item itemData) {
        itemData.setId(getNextId());
        db.put(itemData.getId(), itemData);
        return itemData;
    }

    @Override
    public Item update(Item itemData) {
        db.put(itemData.getId(), itemData);
        return itemData;
    }

    @Override
    public List<Item> getList(long userId) {
        return db.values().stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public Item getById(long itemId) {
        return db.get(itemId);
    }

    @Override
    public List<Item> searchAvailableItems(String text) {
        String lowerText = text.toLowerCase();
        return db.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getIsAvailable()) &&
                        (item.getName().toLowerCase().contains(lowerText) ||
                                item.getDescription().toLowerCase().contains(lowerText)))
                .collect(Collectors.toList());
    }

    @Override
    public Boolean exists(long itemId) {
        return db.containsKey(itemId);
    }

    @Override
    public Boolean isOwnership(long itemId, long userId) {
        Item item = db.get(itemId);
        return item != null && item.getOwner().getId().equals(userId);
    }
}