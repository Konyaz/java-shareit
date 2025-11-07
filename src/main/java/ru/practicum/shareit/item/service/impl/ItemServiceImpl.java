package ru.practicum.shareit.item.service.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.CommentRepository;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dao.CommentMapper;
import ru.practicum.shareit.item.dao.ItemMapper;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.UserRepository;
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

    @Override
    @Transactional
    public ItemDto create(@Valid ItemCreateDto itemData, long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id={} не найден", userId);
                    return new NotFoundException(String.format("Пользователь с id=%s не найден", userId));
                });

        Item item = Item.builder()
                .name(itemData.getName())
                .description(itemData.getDescription())
                .isAvailable(itemData.getAvailable())
                .owner(owner)
                .build();

        Item savedItem = itemRepository.save(item);
        log.info("Создана новая вещь с ID: {} для пользователя с ID: {}", savedItem.getId(), userId);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto update(@Valid ItemUpdateDto itemData, long itemId, long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id={} не найден", userId);
                    return new NotFoundException(String.format("Пользователь с id=%s не найден", userId));
                });

        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.error("Предмет с id={} не найден", itemId);
                    return new NotFoundException(String.format("Предмет с id=%s не найден", itemId));
                });

        if (!existingItem.getOwner().getId().equals(userId)) {
            log.error("Пользователь с id={} не является владельцем вещи с id={}", userId, itemId);
            throw new AccessDeniedException(String.format(
                    "Пользователь с id=%s не является владельцем вещи с id=%s", userId, itemId));
        }

        if (itemData.getName() != null) {
            existingItem.setName(itemData.getName());
        }
        if (itemData.getDescription() != null) {
            existingItem.setDescription(itemData.getDescription());
        }
        if (itemData.getAvailable() != null) {
            existingItem.setIsAvailable(itemData.getAvailable());
        }

        Item updatedItem = itemRepository.save(existingItem);
        log.info("Обновлена вещь с ID: {}", itemId);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public List<ItemWithBookingsDto> getList(long userId) {
        if (!userRepository.existsById(userId)) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException(String.format("Пользователь с id=%s не найден", userId));
        }

        List<Item> items = itemRepository.findByOwnerIdOrderById(userId);
        log.debug("Получен список вещей пользователя с ID: {}, количество: {}", userId, items.size());

        return items.stream()
                .map(this::enrichItemWithBookingsAndComments)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDetailedDto retrieve(long itemId, long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.error("Предмет с id={} не найден", itemId);
                    return new NotFoundException(String.format("Предмет с id=%s не найден", itemId));
                });
        log.debug("Получена вещь с ID: {} для пользователя с ID: {}", itemId, userId);
        return convertToDetailedDto(item);
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            log.debug("Поиск с пустым текстом, возвращаем пустой список");
            return Collections.emptyList();
        }

        String formattedText = text.toLowerCase();
        List<Item> items = itemRepository.searchAvailableItems(formattedText);
        log.debug("Выполнен поиск по тексту '{}', найдено: {} вещей", text, items.size());

        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(long itemId, long userId, CommentCreateDto commentCreateDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.error("Предмет с id={} не найден", itemId);
                    return new NotFoundException(String.format("Предмет с id=%s не найден", itemId));
                });

        User author = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id={} не найден", userId);
                    return new NotFoundException(String.format("Пользователь с id=%s не найден", userId));
                });

        boolean hasCompletedBooking = bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(
                userId, itemId, BookingStatus.APPROVED, LocalDateTime.now());

        if (!hasCompletedBooking) {
            log.error("Пользователь с id={} не брал в аренду вещь с id={} или аренда не завершена", userId, itemId);
            throw new ValidationException("Нельзя оставить комментарий к вещи, которую вы не брали в аренду или аренда еще не завершена");
        }

        Comment comment = Comment.builder()
                .text(commentCreateDto.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();

        Comment savedComment = commentRepository.save(comment);
        log.info("Добавлен комментарий к вещи с ID: {} от пользователя с ID: {}", itemId, userId);

        return CommentMapper.toCommentDto(savedComment);
    }

    private ItemWithBookingsDto enrichItemWithBookingsAndComments(Item item) {
        ItemWithBookingsDto.ItemWithBookingsDtoBuilder itemBuilder = ItemWithBookingsDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getIsAvailable())
                .ownerId(item.getOwner() != null ? item.getOwner().getId() : null);

        LocalDateTime now = LocalDateTime.now();

        List<Booking> lastBookings = bookingRepository.findLastBookingForItem(item.getId(), now);
        List<Booking> nextBookings = bookingRepository.findNextBookingForItem(item.getId(), now);
        List<Booking> currentBookings = bookingRepository.findCurrentBookingForItem(item.getId(), now);

        if (!currentBookings.isEmpty()) {
            itemBuilder.lastBooking(BookingMapper.toBookingInfoDto(currentBookings.get(0)));
        } else if (!lastBookings.isEmpty()) {
            itemBuilder.lastBooking(BookingMapper.toBookingInfoDto(lastBookings.get(0)));
        }

        if (!nextBookings.isEmpty()) {
            itemBuilder.nextBooking(BookingMapper.toBookingInfoDto(nextBookings.get(0)));
        }

        List<Comment> comments = commentRepository.findByItemIdWithAuthor(item.getId());
        List<CommentDto> commentDtos = comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
        itemBuilder.comments(commentDtos);

        return itemBuilder.build();
    }

    private ItemDetailedDto convertToDetailedDto(Item item) {
        ItemDetailedDto.ItemDetailedDtoBuilder dtoBuilder = ItemDetailedDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getIsAvailable())
                .ownerId(item.getOwner() != null ? item.getOwner().getId() : null);

        LocalDateTime now = LocalDateTime.now();
        List<Booking> lastBookings = bookingRepository.findLastBookingForItem(item.getId(), now);
        List<Booking> nextBookings = bookingRepository.findNextBookingForItem(item.getId(), now);
        List<Booking> currentBookings = bookingRepository.findCurrentBookingForItem(item.getId(), now);

        if (!currentBookings.isEmpty()) {
            dtoBuilder.lastBooking(BookingMapper.toBookingInfoDto(currentBookings.get(0)));
        } else if (!lastBookings.isEmpty()) {
            dtoBuilder.lastBooking(BookingMapper.toBookingInfoDto(lastBookings.get(0)));
        }

        if (!nextBookings.isEmpty()) {
            dtoBuilder.nextBooking(BookingMapper.toBookingInfoDto(nextBookings.get(0)));
        }

        List<Comment> comments = commentRepository.findByItemIdWithAuthor(item.getId());
        List<CommentDto> commentDtos = comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
        dtoBuilder.comments(commentDtos);

        return dtoBuilder.build();
    }
}