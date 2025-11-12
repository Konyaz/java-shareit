package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreateRequestDto {
    // ID предмета для бронирования
    private long itemId;
    // Дата и время начала бронирования (должна быть в настоящем или будущем)
    @FutureOrPresent
    private LocalDateTime start;
    // Дата и время окончания бронирования (должна быть в будущем)
    @Future
    private LocalDateTime end;
}