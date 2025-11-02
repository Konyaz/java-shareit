package ru.practicum.shareit.item.service.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.dao.ItemMapper;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemDao dao;
    private final UserDao userDao;

    @Override
    public ItemDto create(@Valid ItemCreateDto itemData, long userId) {
        if (!userDao.exists(userId)) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException(String.format("Пользователь с id=%s не найден", userId));
        }

        User owner = userDao.getById(userId);
        Item item = ItemMapper.toItem(itemData);
        item.setOwner(owner);
        return ItemMapper.toItemDto(dao.create(item));
    }

    @Override
    public ItemDto update(@Valid ItemDto itemData, long itemId, long userId) {
        if (!userDao.exists(userId)) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException(String.format("Пользователь с id=%s не найден", userId));
        }
        if (!dao.exists(itemId)) {
            log.error("Предмет с id={} не найден", itemId);
            throw new NotFoundException(String.format("Предмет с id=%s не найден", itemId));
        }
        if (!dao.isOwnership(itemId, userId)) {
            log.error("Пользователь с id={} не является владельцем вещи с id={}", userId, itemId);
            throw new AccessDeniedException(String.format(
                    "Пользователь с id=%s не является владельцем вещи с id=%s", userId, itemId));
        }

        Item itemToUpdate = ItemMapper.toItem(itemData);
        Item existedItem = dao.getById(itemId);

        itemToUpdate.setId(existedItem.getId());

        if (itemToUpdate.getName() == null || itemToUpdate.getName().isBlank()) {
            itemToUpdate.setName(existedItem.getName());
        }
        if (itemToUpdate.getOwner() == null) {
            itemToUpdate.setOwner(existedItem.getOwner());
        }
        if (itemToUpdate.getDescription() == null || itemToUpdate.getDescription().isBlank()) {
            itemToUpdate.setDescription(existedItem.getDescription());
        }
        if (itemToUpdate.getIsAvailable() == null) {
            itemToUpdate.setIsAvailable(existedItem.getIsAvailable());
        }

        return ItemMapper.toItemDto(dao.update(itemToUpdate));
    }

    @Override
    public List<ItemDto> getList(long userId) {
        if (!userDao.exists(userId)) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException(String.format("Пользователь с id=%s не найден", userId));
        }

        return dao.getList(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto retrieve(long itemId, long userId) {
        if (!dao.exists(itemId)) {
            log.error("Предмет с id={} не найден", itemId);
            throw new NotFoundException(String.format("Предмет с id=%s не найден", itemId));
        }

        return ItemMapper.toItemDto(dao.getById(itemId));
    }

    @Override
    public List<ItemDto> search(String text) {

        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String formattedText = text.toLowerCase();

        return dao.searchAvailableItems(formattedText).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}