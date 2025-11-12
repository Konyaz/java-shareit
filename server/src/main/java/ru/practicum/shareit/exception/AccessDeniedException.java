package ru.practicum.shareit.exception;

public class AccessDeniedException extends RuntimeException {
    // Исключение для случаев отказа в доступе
    public AccessDeniedException(String message) {
        super(message);
    }
}