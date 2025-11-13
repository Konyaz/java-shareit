package ru.practicum.shareit.request.dto;

import lombok.Data;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemRequestDto {
    // ID запроса
    private Long id;
    // Описание запроса
    private String description;
    // Дата и время создания запроса
    private LocalDateTime created;
    // Список предметов, созданных в ответ на запрос
    private List<ItemDto> items;
}