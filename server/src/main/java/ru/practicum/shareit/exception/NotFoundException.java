package ru.practicum.shareit.exception;

public class NotFoundException extends RuntimeException {
    // Исключение для случаев когда объект не найден
    public NotFoundException(String message) {
        super(message);
    }
}