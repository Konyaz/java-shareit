package ru.practicum.shareit.item.dto;

import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

@Data
public class ItemWithBookingsDto {
    // ID предмета
    private Long id;
    // Название предмета
    private String name;
    // Описание предмета
    private String description;
    // Доступность предмета для бронирования
    private Boolean available;
    // Информация о последнем бронировании
    private BookingDto lastBooking;
    // Информация о следующем бронировании
    private BookingDto nextBooking;
    // Список комментариев к предмету
    private List<CommentDto> comments;
}