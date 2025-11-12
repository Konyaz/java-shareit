package ru.practicum.shareit.booking.dao;

public enum Status {
    // Ожидание подтверждения
    WAITING,
    // Подтверждено
    APPROVED,
    // Отклонено
    REJECTED,
    // Отменено
    CANCELED
}