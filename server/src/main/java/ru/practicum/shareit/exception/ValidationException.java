package ru.practicum.shareit.exception;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {
    // Поле в котором произошла ошибка валидации
    private final String field;
    // Причина ошибки валидации
    private final String reason;

    // Конструктор с сообщением об ошибке
    public ValidationException(String message) {
        super(message);
        this.field = null;
        this.reason = null;
    }

    // Конструктор с указанием поля и причины ошибки
    public ValidationException(String field, String reason) {
        super(String.format("Ошибка валидации поля '%s': %s", field, reason));
        this.field = field;
        this.reason = reason;
    }
}