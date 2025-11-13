package ru.practicum.shareit.booking.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingCreateDto {
    // Дата и время начала бронирования
    private LocalDateTime start;
    // Дата и время окончания бронирования
    private LocalDateTime end;
    // ID предмета для бронирования
    private Long itemId;
}