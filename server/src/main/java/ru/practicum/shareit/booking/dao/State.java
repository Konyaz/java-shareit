package ru.practicum.shareit.booking.dao;

public enum State {
    // Все бронирования
    ALL,
    // Текущие бронирования
    CURRENT,
    // Завершенные бронирования
    PAST,
    // Будущие бронирования
    FUTURE,
    // Бронирования в ожидании подтверждения
    WAITING,
    // Отклоненные бронирования
    REJECTED
}