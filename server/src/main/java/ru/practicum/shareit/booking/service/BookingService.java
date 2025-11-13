package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    // Создание нового бронирования
    BookingDto create(Long userId, BookingCreateDto bookingCreateDto);

    // Подтверждение или отклонение бронирования
    BookingDto approve(Long userId, Long bookingId, Boolean approved);

    // Получение информации о бронировании по ID
    BookingDto get(Long userId, Long bookingId);

    // Получение всех бронирований пользователя с фильтрацией по статусу
    List<BookingDto> getAllByUser(Long userId, String state, Integer from, Integer size);

    // Получение всех бронирований для предметов владельца с фильтрацией по статусу
    List<BookingDto> getAllByOwner(Long userId, String state, Integer from, Integer size);
}