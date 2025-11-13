package ru.practicum.shareit.booking.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
        validateBookingDates(bookingCreateDto.getStart(), bookingCreateDto.getEnd());

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

        // Валидация параметров пагинации
        validatePaginationParameters(from, size);

        // Создаем пагинацию с правильным смещением
        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "start"));
        List<Booking> bookings;

        // Обработка различных состояний бронирований
        switch (state) {
            case "ALL":
                bookings = bookingRepository.findByBookerIdWithPagination(userId, pageable);
                break;
            case "CURRENT":
                bookings = bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfterWithPagination(
                        userId, LocalDateTime.now(), LocalDateTime.now(), pageable);
                break;
            case "PAST":
                bookings = bookingRepository.findByBookerIdAndEndIsBeforeWithPagination(
                        userId, LocalDateTime.now(), pageable);
                break;
            case "FUTURE":
                bookings = bookingRepository.findByBookerIdAndStartIsAfterWithPagination(
                        userId, LocalDateTime.now(), pageable);
                break;
            case "WAITING":
                bookings = bookingRepository.findByBookerIdAndStatusIsWithPagination(
                        userId, Status.WAITING, pageable);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByBookerIdAndStatusIsWithPagination(
                        userId, Status.REJECTED, pageable);
                break;
            default:
                throw new ValidationException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    // Получение всех бронирований для предметов владельца с фильтрацией по статусу
    @Override
    public List<BookingDto> getAllByOwner(Long userId, String state, Integer from, Integer size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        // Валидация параметров пагинации
        validatePaginationParameters(from, size);

        // Получаем список ID всех предметов владельца
        List<Long> itemIds = itemRepository.findByOwnerId(userId).stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        if (itemIds.isEmpty()) {
            return List.of();
        }

        // Создаем пагинацию с правильным смещением
        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "start"));
        List<Booking> bookings;

        // Обработка различных состояний бронирований для предметов владельца
        switch (state) {
            case "ALL":
                bookings = bookingRepository.findByItemIdInWithPagination(itemIds, pageable);
                break;
            case "CURRENT":
                bookings = bookingRepository.findByItemIdInAndStartIsBeforeAndEndIsAfterWithPagination(
                        itemIds, LocalDateTime.now(), LocalDateTime.now(), pageable);
                break;
            case "PAST":
                bookings = bookingRepository.findByItemIdInAndEndIsBeforeWithPagination(
                        itemIds, LocalDateTime.now(), pageable);
                break;
            case "FUTURE":
                bookings = bookingRepository.findByItemIdInAndStartIsAfterWithPagination(
                        itemIds, LocalDateTime.now(), pageable);
                break;
            case "WAITING":
                bookings = bookingRepository.findByItemIdInAndStatusIsWithPagination(
                        itemIds, Status.WAITING, pageable);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByItemIdInAndStatusIsWithPagination(
                        itemIds, Status.REJECTED, pageable);
                break;
            default:
                throw new ValidationException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    // Валидация параметров пагинации
    private void validatePaginationParameters(Integer from, Integer size) {
        if (from < 0) {
            throw new ValidationException("Параметр 'from' не может быть отрицательным");
        }
        if (size <= 0) {
            throw new ValidationException("Параметр 'size' должен быть положительным");
        }
    }

    // Валидация дат бронирования
    private void validateBookingDates(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();

        if (start.isBefore(now)) {
            throw new ValidationException("Дата начала бронирования не может быть в прошлом");
        }
        if (end.isBefore(start) || end.equals(start)) {
            throw new ValidationException("Дата окончания бронирования должна быть позже даты начала");
        }
    }
}