package ru.practicum.shareit.item.dto;

import lombok.Data;

@Data
public class ItemCreateDto {
    // ID предмета
    private Long id;
    // ID владельца предмета
    private Long ownerId;
    // ID запроса на предмет
    private Long requestId;
    // Название предмета
    private String name;
    // Описание предмета
    private String description;
    // Доступность предмета для бронирования
    private Boolean available;
}