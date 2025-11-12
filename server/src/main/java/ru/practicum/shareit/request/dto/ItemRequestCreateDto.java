package ru.practicum.shareit.request.dto;

import lombok.Data;

@Data
public class ItemRequestCreateDto {
    // Описание запроса на предмет
    private String description;
}