package ru.practicum.shareit.exception;

public class DatabaseUniqueConstraintException extends RuntimeException {
    // Исключение для случаев нарушения уникальности в базе данных
    public DatabaseUniqueConstraintException(String message) {
        super(message);
    }
}