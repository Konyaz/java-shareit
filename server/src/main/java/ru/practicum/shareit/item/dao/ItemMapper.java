package ru.practicum.shareit.item.dao;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    // Маппинг объекта Item в ItemDto
    @Mapping(source = "owner.id", target = "ownerId")
    @Mapping(source = "isAvailable", target = "available")
    ItemDto toItemDto(Item item);

    // Маппинг объекта ItemDto в Item
    @Mapping(source = "ownerId", target = "owner", qualifiedByName = "mapOwnerIdToUser")
    @Mapping(source = "available", target = "isAvailable")
    Item toItem(ItemDto itemDto);

    // Маппинг объекта ItemCreateDto в Item
    @Mapping(source = "ownerId", target = "owner", qualifiedByName = "mapOwnerIdToUser")
    @Mapping(source = "available", target = "isAvailable")
    Item toItem(ItemCreateDto itemDto);

    // Маппинг ID владельца в объект User
    @Named("mapOwnerIdToUser")
    default User mapOwnerIdToUser(Long ownerId) {
        if (ownerId == null) {
            return null;
        }
        User owner = new User();
        owner.setId(ownerId);
        return owner;
    }
}