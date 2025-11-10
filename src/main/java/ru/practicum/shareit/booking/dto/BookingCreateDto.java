package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreateDto {

    @NotNull(message = "ID вещи обязателен для бронирования")
    private Long itemId;

    @NotNull(message = "Дата начала бронирования обязательна")
    private LocalDateTime start;

    @NotNull(message = "Дата окончания бронирования обязательна")
    private LocalDateTime end;

    @AssertTrue(message = "Дата окончания бронирования должна быть после даты начала")
    public boolean isEndAfterStart() {
        return end != null && start != null && end.isAfter(start);
    }
}