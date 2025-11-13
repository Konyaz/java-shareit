package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.booking.dao.Status;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Data
public class BookingDto {
    // ID бронирования
    private Long id;
    // Дата и время начала бронирования
    private LocalDateTime start;
    // Дата и время окончания бронирования
    private LocalDateTime end;
    // Информация о предмете
    private ItemDto item;
    // Информация о бронирующем
    private UserDto booker;
    // Статус бронирования
    private Status status;
}