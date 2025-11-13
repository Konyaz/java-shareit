package ru.practicum.shareit.request.dao;

import org.mapstruct.Mapper;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

@Mapper(componentModel = "spring")
public interface ItemRequestMapper {
    // Маппинг объекта ItemRequest в ItemRequestDto
    ItemRequestDto toItemRequestDto(ItemRequest itemRequest);

    // Маппинг объекта ItemRequestCreateDto в ItemRequest
    ItemRequest toItemRequest(ItemRequestCreateDto itemRequestDto);
}