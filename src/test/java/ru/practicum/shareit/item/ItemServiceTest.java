package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.impl.ItemServiceImpl;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemDao itemDao;

    @Mock
    private UserDao userDao;

    private ItemServiceImpl itemService;

    @BeforeEach
    void setUp() {
        itemService = new ItemServiceImpl(itemDao, userDao);
    }

    // Тест создания предмета с валидными данными - должен вернуть ItemDto
    @Test
    void createItem_ValidData_ReturnsItemDto() {
        ItemCreateDto itemCreateDto = new ItemCreateDto();
        itemCreateDto.setName("Item");
        itemCreateDto.setDescription("Description");
        itemCreateDto.setAvailable(true);

        User owner = new User();
        owner.setId(1L);
        owner.setName("Owner");

        Item item = new Item();
        item.setId(1L);
        item.setName("Item");
        item.setDescription("Description");
        item.setIsAvailable(true);
        item.setOwner(owner);

        when(userDao.exists(1L)).thenReturn(true);
        when(userDao.getById(1L)).thenReturn(owner);
        when(itemDao.create(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.create(itemCreateDto, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Item", result.getName());
        assertEquals("Description", result.getDescription());
        assertTrue(result.getAvailable());
    }

    // Тест создания предмета для несуществующего пользователя - должен выбросить NotFoundException
    @Test
    void createItem_UserNotFound_ThrowsException() {
        ItemCreateDto itemCreateDto = new ItemCreateDto();
        itemCreateDto.setName("Item");
        itemCreateDto.setDescription("Description");
        itemCreateDto.setAvailable(true);

        when(userDao.exists(999L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> itemService.create(itemCreateDto, 999L));
    }

    // Тест обновления предмета не владельцем - должен выбросить AccessDeniedException
    @Test
    void updateItem_NotOwner_ThrowsException() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("UpdatedItem");

        User owner = new User();
        owner.setId(1L);

        Item existingItem = new Item();
        existingItem.setId(1L);
        existingItem.setOwner(owner);

        when(userDao.exists(2L)).thenReturn(true);
        when(itemDao.exists(1L)).thenReturn(true);
        when(itemDao.isOwnership(1L, 2L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> itemService.update(itemDto, 1L, 2L));
    }

    // Тест поиска предметов по тексту - должен вернуть список найденных предметов
    @Test
    void searchItems_ValidText_ReturnsItems() {
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setId(1L);
        item.setName("Drill");
        item.setDescription("Powerful drill");
        item.setIsAvailable(true);
        item.setOwner(owner);

        when(itemDao.searchAvailableItems("drill")).thenReturn(List.of(item));

        List<ItemDto> results = itemService.search("drill");

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals("Drill", results.get(0).getName());
    }

    // Тест поиска с пустым текстом - должен вернуть пустой список
    @Test
    void searchItems_EmptyText_ReturnsEmptyList() {
        List<ItemDto> results = itemService.search("");

        assertTrue(results.isEmpty());
    }
}