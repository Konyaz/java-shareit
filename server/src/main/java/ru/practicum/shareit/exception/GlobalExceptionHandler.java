package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Обработка исключения "Не найдено"
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(NotFoundException e) {
        log.error("NotFoundException: {}", e.getMessage());
        return new ErrorResponse(e.getMessage(), LocalDateTime.now());
    }

    // Обработка исключения нарушения уникальности в БД
    @ExceptionHandler(DatabaseUniqueConstraintException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicateEmail(DatabaseUniqueConstraintException e) {
        log.error("DatabaseUniqueConstraintException: {}", e.getMessage());
        return new ErrorResponse(e.getMessage(), LocalDateTime.now());
    }

    // Обработка исключения валидации
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(ValidationException e) {
        log.error("ValidationException: {}", e.getMessage());
        return new ErrorResponse(e.getMessage(), LocalDateTime.now());
    }

    // Обработка исключения отказа в доступе
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDenied(AccessDeniedException e) {
        log.error("AccessDeniedException: {}", e.getMessage());
        return new ErrorResponse(e.getMessage(), LocalDateTime.now());
    }

    // Обработка исключения невалидных аргументов метода
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String errorMessage = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        log.error("MethodArgumentNotValidException: {}", errorMessage);
        return new ErrorResponse(errorMessage, LocalDateTime.now());
    }

    // Обработка отсутствующего заголовка запроса
    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingRequestHeader(MissingRequestHeaderException e) {
        log.error("MissingRequestHeaderException: {}", e.getMessage());
        return new ErrorResponse("Отсутствует обязательный заголовок: " + e.getHeaderName(), LocalDateTime.now());
    }

    // Обработка отсутствующего параметра запроса
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingServletRequestParameter(MissingServletRequestParameterException e) {
        log.error("MissingServletRequestParameterException: {}", e.getMessage());
        return new ErrorResponse("Отсутствует обязательный параметр: " + e.getParameterName(), LocalDateTime.now());
    }

    // Обработка всех остальных исключений
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleOtherExceptions(Exception e) {
        log.error("Internal server error: ", e);
        return new ErrorResponse("Внутренняя ошибка сервера", LocalDateTime.now());
    }
}