package ru.practicum.shareit.exception;

public class DatabaseUniqueConstraintException extends RuntimeException {
    public DatabaseUniqueConstraintException(String message) {
        super(message);
    }
}