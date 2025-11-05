package ru.practicum.shareit.exception;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {
    private final String field;
    private final String reason;

    public ValidationException(String message) {
        super(message);
        this.field = null;
        this.reason = null;
    }

    public ValidationException(String field, String reason) {
        super(String.format("Ошибка валидации поля '%s': %s", field, reason));
        this.field = field;
        this.reason = reason;
    }
}