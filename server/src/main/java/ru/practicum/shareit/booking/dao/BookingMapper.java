package ru.practicum.shareit.booking.dao;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "item", source = "item", qualifiedByName = "mapItemToItemDto")
    @Mapping(target = "booker", source = "booker", qualifiedByName = "mapUserToUserDto")
    BookingDto toBookingDto(Booking booking);

    // Маппинг объекта Item в ItemDto
    @Named("mapItemToItemDto")
    default ItemDto mapItemToItemDto(ru.practicum.shareit.item.model.Item item) {
        if (item == null) {
            return null;
        }

        ItemDto itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setAvailable(item.getIsAvailable());

        if (item.getOwner() != null) {
            itemDto.setOwnerId(item.getOwner().getId());
        }

        return itemDto;
    }

    // Маппинг объекта User в UserDto
    @Named("mapUserToUserDto")
    default UserDto mapUserToUserDto(ru.practicum.shareit.user.model.User user) {
        if (user == null) {
            return null;
        }

        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());

        return userDto;
    }
}