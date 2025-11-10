package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {

    /**
     * Создание нового бронирования
     *
     * @param bookingCreateDto данные для создания бронирования
     * @param userId           ID пользователя
     * @return созданное бронирование
     */
    BookingDto create(BookingCreateDto bookingCreateDto, Long userId);

    /**
     * Подтверждение или отклонение бронирования
     *
     * @param bookingId ID бронирования
     * @param userId    ID пользователя
     * @param approved  true - подтвердить, false - отклонить
     * @return обновленное бронирование
     */
    BookingDto approve(Long bookingId, Long userId, Boolean approved);

    /**
     * Отмена бронирования создателем
     *
     * @param bookingId ID бронирования
     * @param userId    ID пользователя
     * @return отмененное бронирование
     */
    BookingDto cancel(Long bookingId, Long userId);

    /**
     * Получение бронирования по ID
     *
     * @param bookingId ID бронирования
     * @param userId    ID пользователя
     * @return бронирование
     */
    BookingDto getById(Long bookingId, Long userId);

    /**
     * Получение всех бронирований пользователя
     *
     * @param userId ID пользователя
     * @param state  состояние бронирования
     * @return список бронирований
     */
    List<BookingDto> getUserBookings(Long userId, String state);

    /**
     * Получение всех бронирований для вещей пользователя
     *
     * @param userId ID пользователя
     * @param state  состояние бронирования
     * @return список бронирований
     */
    List<BookingDto> getOwnerBookings(Long userId, String state);
}