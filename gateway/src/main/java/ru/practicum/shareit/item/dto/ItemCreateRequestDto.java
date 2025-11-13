package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ItemCreateRequestDto {
    // ID запроса (должен быть положительным)
    @Positive(message = "'requestId' must be positive")
    private Long requestId;

    // Название предмета (обязательное поле)
    @NotBlank(message = "'name' is required")
    private String name;

    // Описание предмета (обязательное поле)
    @NotBlank(message = "'description' is required")
    private String description;

    // Доступность предмета для бронирования (обязательное поле)
    @NotNull(message = "'available' is required")
    private Boolean available;
}