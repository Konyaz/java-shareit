package ru.practicum.shareit.booking.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dao.BookingMapper;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dao.Status;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;

    // Создание нового бронирования
    @Override
    @Transactional
    public BookingDto create(Long userId, BookingCreateDto bookingCreateDto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        Item item = itemRepository.findById(bookingCreateDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + bookingCreateDto.getItemId() + " не найдена"));

        // Проверка доступности предмета для бронирования
        if (!item.getIsAvailable()) {
            throw new ValidationException("Вещь с id=" + bookingCreateDto.getItemId() + " недоступна для бронирования");
        }

        // Проверка что пользователь не бронирует свою вещь
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Нельзя забронировать свою вещь");
        }

        // Проверка корректности дат бронирования
        if (bookingCreateDto.getEnd().isBefore(bookingCreateDto.getStart()) ||
                bookingCreateDto.getEnd().equals(bookingCreateDto.getStart())) {
            throw new ValidationException("Дата окончания бронирования должна быть позже даты начала");
        }

        Booking booking = new Booking();
        booking.setStart(bookingCreateDto.getStart());
        booking.setEnd(bookingCreateDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Status.WAITING);

        return bookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    // Подтверждение или отклонение бронирования
    @Override
    @Transactional
    public BookingDto approve(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id=" + bookingId + " не найдено"));

        // Проверка прав доступа - только владелец предмета может подтверждать бронирование
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Подтверждать бронирование может только владелец вещи");
        }

        // Проверка что бронирование еще не обработано
        if (!booking.getStatus().equals(Status.WAITING)) {
            throw new ValidationException("Статус бронирования уже установлен");
        }

        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        return bookingMapper.toBookingDto(booking);
    }

    // Получение информации о бронировании по ID
    @Override
    public BookingDto get(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id=" + bookingId + " не найдено"));

        // Проверка что пользователь имеет доступ к просмотру бронирования
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException(
                    "Просматривать бронирование может только автор бронирования или владелец вещи");
        }

        return bookingMapper.toBookingDto(booking);
    }

    // Получение всех бронирований пользователя с фильтрацией по статусу
    @Override
    public List<BookingDto> getAllByUser(Long userId, String state, Integer from, Integer size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        List<Booking> bookings;

        // Обработка различных состояний бронирований
        switch (state) {
            case "ALL":
                bookings = bookingRepository.findByBookerId(userId, sort);
                break;
            case "CURRENT":
                bookings = bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(userId, LocalDateTime.now(),
                        LocalDateTime.now(), sort);
                break;
            case "PAST":
                bookings = bookingRepository.findByBookerIdAndEndIsBefore(userId, LocalDateTime.now(), sort);
                break;
            case "FUTURE":
                bookings = bookingRepository.findByBookerIdAndStartIsAfter(userId, LocalDateTime.now(), sort);
                break;
            case "WAITING":
                bookings = bookingRepository.findByBookerIdAndStatusIs(userId, Status.WAITING, sort);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByBookerIdAndStatusIs(userId, Status.REJECTED, sort);
                break;
            default:
                throw new ValidationException("Unknown state: " + state);
        }

        // Применяем пагинацию
        int startIndex = from;
        int endIndex = Math.min(startIndex + size, bookings.size());

        if (startIndex > bookings.size()) {
            return List.of();
        }

        return bookings.subList(startIndex, endIndex).stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    // Получение всех бронирований для предметов владельца с фильтрацией по статусу
    @Override
    public List<BookingDto> getAllByOwner(Long userId, String state, Integer from, Integer size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        // Получаем список ID всех предметов владельца
        List<Long> itemIds = itemRepository.findByOwnerId(userId).stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        if (itemIds.isEmpty()) {
            return List.of();
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        List<Booking> bookings;

        // Обработка различных состояний бронирований для предметов владельца
        switch (state) {
            case "ALL":
                bookings = bookingRepository.findByItemIdIn(itemIds, sort);
                break;
            case "CURRENT":
                bookings = bookingRepository.findByItemIdInAndStartIsBeforeAndEndIsAfter(itemIds, LocalDateTime.now(),
                        LocalDateTime.now(), sort);
                break;
            case "PAST":
                bookings = bookingRepository.findByItemIdInAndEndIsBefore(itemIds, LocalDateTime.now(), sort);
                break;
            case "FUTURE":
                bookings = bookingRepository.findByItemIdInAndStartIsAfter(itemIds, LocalDateTime.now(), sort);
                break;
            case "WAITING":
                bookings = bookingRepository.findByItemIdInAndStatusIs(itemIds, Status.WAITING, sort);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByItemIdInAndStatusIs(itemIds, Status.REJECTED, sort);
                break;
            default:
                throw new ValidationException("Unknown state: " + state);
        }

        // Применяем пагинацию
        int startIndex = from;
        int endIndex = Math.min(startIndex + size, bookings.size());

        if (startIndex > bookings.size()) {
            return List.of();
        }

        return bookings.subList(startIndex, endIndex).stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }
}