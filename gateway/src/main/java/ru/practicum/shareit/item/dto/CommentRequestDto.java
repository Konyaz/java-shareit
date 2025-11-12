package ru.practicum.shareit.item.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentRequestDto {
    // ID комментария
    private Long id;
    // Текст комментария
    private String text;
    // Имя автора комментария
    private String authorName;
    // Дата и время создания комментария
    private LocalDateTime created;
}