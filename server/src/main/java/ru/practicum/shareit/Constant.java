package ru.practicum.shareit;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constant {
    // Заголовок для передачи ID пользователя в запросах
    public static final String OWNER_HEADER = "X-Sharer-User-Id";
}