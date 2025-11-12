package ru.practicum.shareit.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    // Сообщение об ошибке
    private final String error;
    // Время возникновения ошибки
    private final LocalDateTime timestamp;
}