package ru.practicum.shareit.item.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.CommentMapper;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dao.ItemMapper;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    // Создание нового предмета
    @Override
    @Transactional
    public ItemDto create(ItemCreateDto itemData, long userId) {
        User owner = userRepository.findById(userId).orElseThrow(() -> {
            log.error("Пользователь с id={} не найден", userId);
            return new NotFoundException(String.format("Пользователь с id=%s не найден", userId));
        });
        Item item = itemMapper.toItem(itemData);
        item.setOwner(owner);

        // Если указан ID запроса, связываем предмет с запросом
        if (itemData.getRequestId() != null) {
            long requestId = itemData.getRequestId();
            ItemRequest request = itemRequestRepository.findById(requestId).orElseThrow(() -> {
                log.error("Запрос с id={} не найден", requestId);
                return new NotFoundException(String.format("Запрос с id=%s не найден", requestId));
            });
            item.setRequest(request);
        }

        return itemMapper.toItemDto(itemRepository.save(item));
    }

    // Обновление существующего предмета
    @Override
    @Transactional
    public ItemDto update(ItemDto itemData, long itemId, long userId) {
        Item itemToUpdate = itemMapper.toItem(itemData);
        Item existedItem = itemRepository.findById(itemId).orElseThrow(() -> {
            log.error("Предмет с id={} не найден", itemId);
            return new NotFoundException(String.format("Предмет с id=%s не найден", itemId));
        });
        if (!userRepository.existsById(userId)) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException(String.format("Пользователь с id=%s не найден", userId));
        }
        // Проверка что пользователь является владельцем предмета
        if (existedItem.getOwner().getId() != userId) {
            log.error("Пользователь с id={} не является владельцем вещи с id={}", userId, itemId);
            throw new AccessDeniedException(String.format(
                    "Пользователь с id=%s не является владельцем вещи с id=%s", userId, itemId));
        }

        // Обновление названия если оно указано и не пустое
        if (itemToUpdate.getName() != null && !itemToUpdate.getName().isBlank()) {
            existedItem.setName(itemToUpdate.getName());
        }
        // Обновление описания если оно указано и не пустое
        if (itemToUpdate.getDescription() != null && !itemToUpdate.getDescription().isBlank()) {
            existedItem.setDescription(itemToUpdate.getDescription());
        }
        // Обновление доступности если она указана
        if (itemToUpdate.getIsAvailable() != null) {
            existedItem.setIsAvailable(itemToUpdate.getIsAvailable());
        }

        return itemMapper.toItemDto(existedItem);
    }

    // Получение списка предметов пользователя
    @Override
    public List<ItemDto> getList(long userId) {
        if (!userRepository.existsById(userId)) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException(String.format("Пользователь с id=%s не найден", userId));
        }

        List<Item> items = itemRepository.findByOwnerId(userId);
        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> {
                    ItemDto itemDto = itemMapper.toItemDto(item);

                    // Если пользователь является владельцем вещи, добавляем даты бронирований
                    if (item.getOwner().getId() == userId) {
                        // Получаем последнее бронирование
                        List<Booking> lastBookings = bookingRepository.findLastBookingByItemId(item.getId(), now);
                        if (!lastBookings.isEmpty()) {
                            itemDto.setLastBooking(lastBookings.get(0).getStart());
                        }

                        // Получаем следующее бронирование
                        List<Booking> nextBookings = bookingRepository.findNextBookingByItemId(item.getId(), now);
                        if (!nextBookings.isEmpty()) {
                            itemDto.setNextBooking(nextBookings.get(0).getStart());
                        }
                    }

                    // Добавляем комментарии
                    List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(item.getId());
                    itemDto.setComments(comments.stream()
                            .map(commentMapper::toCommentDto)
                            .collect(Collectors.toList()));

                    return itemDto;
                })
                .collect(Collectors.toList());
    }

    // Получение конкретного предмета по ID
    @Override
    public ItemDto retrieve(long itemId, long userId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> {
            log.error("Предмет с id={} не найден", itemId);
            return new NotFoundException(String.format("Предмет с id=%s не найден", itemId));
        });
        ItemDto itemDto = itemMapper.toItemDto(item);

        // Если пользователь является владельцем вещи, добавляем даты бронирований
        if (item.getOwner().getId() == userId) {
            LocalDateTime now = LocalDateTime.now();

            // Получаем последнее бронирование
            List<Booking> lastBookings = bookingRepository.findLastBookingByItemId(itemId, now);
            if (!lastBookings.isEmpty()) {
                itemDto.setLastBooking(lastBookings.get(0).getStart());
            }

            // Получаем следующее бронирование
            List<Booking> nextBookings = bookingRepository.findNextBookingByItemId(itemId, now);
            if (!nextBookings.isEmpty()) {
                itemDto.setNextBooking(nextBookings.get(0).getStart());
            }
        }

        // Добавляем комментарии
        List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(itemId);
        itemDto.setComments(comments.stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList()));

        return itemDto;
    }

    // Поиск предметов по тексту
    @Override
    public List<ItemDto> search(String text) {
        // Если текст пустой, возвращаем пустой список
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String formattedText = text.toLowerCase();

        return itemRepository.searchAvailableItems(formattedText).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}