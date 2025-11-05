package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ItemCreateDto {
    private Long id;

    private Long ownerId;

    @NotBlank(message = "'name' is required")
    private String name;

    @NotBlank(message = "'description' is required")
    private String description;

    @NotNull(message = "'available' is required")
    private Boolean available;
}