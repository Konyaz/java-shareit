package ru.practicum.shareit.item.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.CommentMapper;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dao.ItemMapper;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User user;
    private Item item;
    private ItemCreateDto itemCreateDto;
    private ItemDto itemDto;
    private ItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");

        item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setIsAvailable(true);
        item.setOwner(user);

        itemCreateDto = new ItemCreateDto();
        itemCreateDto.setName("Test Item");
        itemCreateDto.setDescription("Test Description");
        itemCreateDto.setAvailable(true);

        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);

        itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setDescription("Test Request");
        itemRequest.setAuthor(user);
        itemRequest.setCreated(LocalDateTime.now());
    }

    // Тест успешного создания предмета
    @Test
    void createItem_WithValidData_ShouldReturnItemDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemMapper.toItem(itemCreateDto)).thenReturn(item);
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.create(itemCreateDto, 1L);

        assertNotNull(result);
        assertEquals("Test Item", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertTrue(result.getAvailable());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    // Тест создания предмета для несуществующего пользователя
    @Test
    void createItem_WithNonExistentUser_ShouldThrowNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.create(itemCreateDto, 999L));

        verify(itemRepository, never()).save(any(Item.class));
    }

    // Тест создания предмета с указанием ID запроса
    @Test
    void createItem_WithRequestId_ShouldHandleRequest() {
        ItemCreateDto itemWithRequest = new ItemCreateDto();
        itemWithRequest.setName("Item with Request");
        itemWithRequest.setDescription("Description");
        itemWithRequest.setAvailable(true);
        itemWithRequest.setRequestId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(itemRequest));
        when(itemMapper.toItem(itemWithRequest)).thenReturn(item);
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.create(itemWithRequest, 1L);

        assertNotNull(result);
        verify(itemRepository, times(1)).save(any(Item.class));
        verify(itemRequestRepository, times(1)).findById(1L);
    }

    // Тест создания предмета с несуществующим запросом
    @Test
    void createItem_WithNonExistentRequest_ShouldThrowNotFoundException() {
        ItemCreateDto itemWithRequest = new ItemCreateDto();
        itemWithRequest.setName("Item with Request");
        itemWithRequest.setDescription("Description");
        itemWithRequest.setAvailable(true);
        itemWithRequest.setRequestId(999L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(999L)).thenReturn(Optional.empty());
        when(itemMapper.toItem(itemWithRequest)).thenReturn(item); // Возвращаем валидный item

        assertThrows(NotFoundException.class, () -> itemService.create(itemWithRequest, 1L));

        verify(itemRepository, never()).save(any(Item.class));
    }

    // Тест успешного обновления предмета
    @Test
    void updateItem_WithValidData_ShouldReturnUpdatedItem() {
        ItemDto updateDto = new ItemDto();
        updateDto.setName("Updated Name");
        updateDto.setDescription("Updated Description");
        updateDto.setAvailable(false);

        Item updatedItem = new Item();
        updatedItem.setId(1L);
        updatedItem.setName("Updated Name");
        updatedItem.setDescription("Updated Description");
        updatedItem.setIsAvailable(false);
        updatedItem.setOwner(user);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemMapper.toItem(updateDto)).thenReturn(updatedItem);
        when(itemMapper.toItemDto(item)).thenReturn(updateDto);

        ItemDto result = itemService.update(updateDto, 1L, 1L);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertFalse(result.getAvailable());
    }

    // Тест обновления несуществующего предмета
    @Test
    void updateItem_WithNonExistentItem_ShouldThrowNotFoundException() {
        ItemDto updateDto = new ItemDto();
        updateDto.setName("Updated Name");

        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.update(updateDto, 999L, 1L));
    }

    // Тест обновления предмета несуществующим пользователем
    @Test
    void updateItem_WithNonExistentUser_ShouldThrowNotFoundException() {
        ItemDto updateDto = new ItemDto();
        updateDto.setName("Updated Name");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> itemService.update(updateDto, 1L, 999L));
    }

    // Тест обновления предмета не владельцем
    @Test
    void updateItem_WithWrongOwner_ShouldThrowAccessDeniedException() {
        ItemDto updateDto = new ItemDto();
        updateDto.setName("Updated Name");

        User differentUser = new User();
        differentUser.setId(2L);
        differentUser.setName("Different User");
        differentUser.setEmail("different@example.com");

        Item itemWithDifferentOwner = new Item();
        itemWithDifferentOwner.setId(1L);
        itemWithDifferentOwner.setName("Test Item");
        itemWithDifferentOwner.setDescription("Test Description");
        itemWithDifferentOwner.setIsAvailable(true);
        itemWithDifferentOwner.setOwner(differentUser);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(itemWithDifferentOwner));
        when(userRepository.existsById(1L)).thenReturn(true);

        assertThrows(AccessDeniedException.class, () -> itemService.update(updateDto, 1L, 1L));
    }

    // Тест частичного обновления предмета
    @Test
    void updateItem_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        ItemDto partialUpdate = new ItemDto();
        partialUpdate.setName("Updated Name Only");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemMapper.toItem(partialUpdate)).thenReturn(item);
        when(itemMapper.toItemDto(item)).thenReturn(partialUpdate);

        ItemDto result = itemService.update(partialUpdate, 1L, 1L);

        assertNotNull(result);
        assertEquals("Updated Name Only", result.getName());
    }

    // Тест получения списка предметов пользователя
    @Test
    void getItemsList_WithValidUser_ShouldReturnItemList() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemRepository.findByOwnerId(1L)).thenReturn(List.of(item));
        when(bookingRepository.findLastBookingsByItemIds(anyList(), any(LocalDateTime.class))).thenReturn(new ArrayList<>());
        when(bookingRepository.findNextBookingsByItemIds(anyList(), any(LocalDateTime.class))).thenReturn(new ArrayList<>());
        when(commentRepository.findByItemIdIn(anyList())).thenReturn(new ArrayList<>());
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        List<ItemDto> result = itemService.getList(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Item", result.get(0).getName());
        verify(bookingRepository, times(1)).findLastBookingsByItemIds(anyList(), any(LocalDateTime.class));
        verify(bookingRepository, times(1)).findNextBookingsByItemIds(anyList(), any(LocalDateTime.class));
    }

    // Тест получения списка предметов с бронированиями и комментариями
    @Test
    void getItemsList_WithBookingsAndComments_ShouldReturnItemListWithDetails() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemRepository.findByOwnerId(1L)).thenReturn(List.of(item));

        // Настройка моков для бронирований
        Booking lastBooking = new Booking();
        lastBooking.setId(1L);
        lastBooking.setItem(item);
        lastBooking.setStart(LocalDateTime.now().minusDays(2));
        lastBooking.setEnd(LocalDateTime.now().minusDays(1));

        Booking nextBooking = new Booking();
        nextBooking.setId(2L);
        nextBooking.setItem(item);
        nextBooking.setStart(LocalDateTime.now().plusDays(1));
        nextBooking.setEnd(LocalDateTime.now().plusDays(2));

        when(bookingRepository.findLastBookingsByItemIds(anyList(), any(LocalDateTime.class))).thenReturn(new ArrayList<>(List.of(lastBooking)));
        when(bookingRepository.findNextBookingsByItemIds(anyList(), any(LocalDateTime.class))).thenReturn(new ArrayList<>(List.of(nextBooking)));

        // Настройка моков для комментариев
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setText("Test comment");
        comment.setCreated(LocalDateTime.now());

        when(commentRepository.findByItemIdIn(anyList())).thenReturn(new ArrayList<>(List.of(comment)));
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        List<ItemDto> result = itemService.getList(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository, times(1)).findLastBookingsByItemIds(anyList(), any(LocalDateTime.class));
        verify(bookingRepository, times(1)).findNextBookingsByItemIds(anyList(), any(LocalDateTime.class));
        verify(commentRepository, times(1)).findByItemIdIn(anyList());
    }

    // Тест получения списка предметов несуществующего пользователя
    @Test
    void getItemsList_WithNonExistentUser_ShouldThrowNotFoundException() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> itemService.getList(999L));
    }

    // Тест получения конкретного предмета
    @Test
    void retrieveItem_WithExistingItem_ShouldReturnItemDto() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.findLastBookingsByItemIds(anyList(), any(LocalDateTime.class))).thenReturn(new ArrayList<>());
        when(bookingRepository.findNextBookingsByItemIds(anyList(), any(LocalDateTime.class))).thenReturn(new ArrayList<>());
        when(commentRepository.findByItemIdOrderByCreatedDesc(1L)).thenReturn(new ArrayList<>());
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.retrieve(1L, 1L);

        assertNotNull(result);
        assertEquals("Test Item", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertTrue(result.getAvailable());
        verify(bookingRepository, times(1)).findLastBookingsByItemIds(anyList(), any(LocalDateTime.class));
        verify(bookingRepository, times(1)).findNextBookingsByItemIds(anyList(), any(LocalDateTime.class));
    }

    // Тест получения предмета с бронированиями для владельца
    @Test
    void retrieveItem_WithBookingsForOwner_ShouldReturnItemWithBookingDates() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        // Настройка моков для бронирований
        Booking lastBooking = new Booking();
        lastBooking.setId(1L);
        lastBooking.setItem(item);
        lastBooking.setStart(LocalDateTime.now().minusDays(2));
        lastBooking.setEnd(LocalDateTime.now().minusDays(1));

        Booking nextBooking = new Booking();
        nextBooking.setId(2L);
        nextBooking.setItem(item);
        nextBooking.setStart(LocalDateTime.now().plusDays(1));
        nextBooking.setEnd(LocalDateTime.now().plusDays(2));

        when(bookingRepository.findLastBookingsByItemIds(anyList(), any(LocalDateTime.class))).thenReturn(new ArrayList<>(List.of(lastBooking)));
        when(bookingRepository.findNextBookingsByItemIds(anyList(), any(LocalDateTime.class))).thenReturn(new ArrayList<>(List.of(nextBooking)));
        when(commentRepository.findByItemIdOrderByCreatedDesc(1L)).thenReturn(new ArrayList<>());
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.retrieve(1L, 1L);

        assertNotNull(result);
        verify(bookingRepository, times(1)).findLastBookingsByItemIds(anyList(), any(LocalDateTime.class));
        verify(bookingRepository, times(1)).findNextBookingsByItemIds(anyList(), any(LocalDateTime.class));
    }

    // Тест получения предмета без бронирований для не владельца
    @Test
    void retrieveItem_ForNonOwner_ShouldReturnItemWithoutBookingDates() {
        User differentUser = new User();
        differentUser.setId(2L);
        differentUser.setName("Different User");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemIdOrderByCreatedDesc(1L)).thenReturn(new ArrayList<>());
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.retrieve(1L, 2L);

        assertNotNull(result);
        // Для не владельца не должны вызываться методы поиска бронирований
        verify(bookingRepository, never()).findLastBookingsByItemIds(anyList(), any(LocalDateTime.class));
        verify(bookingRepository, never()).findNextBookingsByItemIds(anyList(), any(LocalDateTime.class));
    }

    // Тест получения несуществующего предмета
    @Test
    void retrieveItem_WithNonExistentItem_ShouldThrowNotFoundException() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.retrieve(999L, 1L));
    }

    // Тест поиска предметов по тексту
    @Test
    void searchItems_WithValidText_ShouldReturnMatchingItems() {
        when(itemRepository.searchAvailableItems("test")).thenReturn(List.of(item));
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        List<ItemDto> result = itemService.search("test");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Item", result.get(0).getName());
    }

    // Тест поиска с пустым текстом
    @Test
    void searchItems_WithEmptyText_ShouldReturnEmptyList() {
        List<ItemDto> result = itemService.search("");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(itemRepository, never()).searchAvailableItems(anyString());
    }

    // Тест поиска с null текстом
    @Test
    void searchItems_WithNullText_ShouldReturnEmptyList() {
        List<ItemDto> result = itemService.search(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(itemRepository, never()).searchAvailableItems(anyString());
    }

    // Тест поиска с пробелами в тексте
    @Test
    void searchItems_WithBlankText_ShouldReturnEmptyList() {
        List<ItemDto> result = itemService.search("   ");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(itemRepository, never()).searchAvailableItems(anyString());
    }

    // Тест обновления с null полями
    @Test
    void updateItem_WithNullFields_ShouldNotUpdateNullFields() {
        ItemDto updateWithNulls = new ItemDto();
        updateWithNulls.setName(null);
        updateWithNulls.setDescription(null);
        updateWithNulls.setAvailable(null);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemMapper.toItem(updateWithNulls)).thenReturn(item);
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.update(updateWithNulls, 1L, 1L);

        assertNotNull(result);
        assertEquals("Test Item", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertTrue(result.getAvailable());
    }

    // Тест обновления с пустым названием
    @Test
    void updateItem_WithBlankName_ShouldNotUpdateBlankName() {
        ItemDto updateWithBlankName = new ItemDto();
        updateWithBlankName.setName("   ");
        updateWithBlankName.setDescription("Valid Description");
        updateWithBlankName.setAvailable(true);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemMapper.toItem(updateWithBlankName)).thenReturn(item);
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.update(updateWithBlankName, 1L, 1L);

        assertNotNull(result);
        assertEquals("Test Item", result.getName());
    }

    // Тест обновления с пустым описанием
    @Test
    void updateItem_WithBlankDescription_ShouldNotUpdateBlankDescription() {
        ItemDto updateWithBlankDescription = new ItemDto();
        updateWithBlankDescription.setName("Valid Name");
        updateWithBlankDescription.setDescription("   ");
        updateWithBlankDescription.setAvailable(true);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemMapper.toItem(updateWithBlankDescription)).thenReturn(item);
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.update(updateWithBlankDescription, 1L, 1L);

        assertNotNull(result);
        assertEquals("Test Description", result.getDescription());
    }

    // Тест получения предмета с комментариями
    @Test
    void retrieveItem_WithComments_ShouldReturnItemWithComments() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.findLastBookingsByItemIds(anyList(), any(LocalDateTime.class))).thenReturn(new ArrayList<>());
        when(bookingRepository.findNextBookingsByItemIds(anyList(), any(LocalDateTime.class))).thenReturn(new ArrayList<>());

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setText("Test comment");
        comment.setCreated(LocalDateTime.now());

        when(commentRepository.findByItemIdOrderByCreatedDesc(1L)).thenReturn(new ArrayList<>(List.of(comment)));
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.retrieve(1L, 1L);

        assertNotNull(result);
        verify(commentRepository, times(1)).findByItemIdOrderByCreatedDesc(1L);
    }
}