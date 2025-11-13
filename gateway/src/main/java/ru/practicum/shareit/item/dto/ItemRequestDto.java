package ru.practicum.shareit.item.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemRequestDto {
    // ID предмета
    private Long id;
    // ID владельца предмета
    private Long ownerId;
    // Название предмета
    private String name;
    // Описание предмета
    private String description;
    // Доступность предмета для бронирования
    private Boolean available;
    // Дата последнего бронирования
    private LocalDateTime lastBooking;
    // Дата следующего бронирования
    private LocalDateTime nextBooking;
    // Список комментариев к предмету
    private List<CommentRequestDto> comments;
}