package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.impl.ItemServiceImpl;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
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

    private ItemServiceImpl itemService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private LocalDateTime fixedTime;

    @BeforeEach
    void setUp() {
        itemService = new ItemServiceImpl(itemRepository, userRepository, bookingRepository, commentRepository);

        fixedTime = LocalDateTime.of(2024, 1, 1, 12, 0);

        // Инициализация пользователей
        owner = new User();
        owner.setId(1L);
        owner.setName("Owner");
        owner.setEmail("owner@example.com");

        booker = new User();
        booker.setId(2L);
        booker.setName("Booker");
        booker.setEmail("booker@example.com");

        // Инициализация вещи
        item = new Item();
        item.setId(1L);
        item.setName("Item");
        item.setDescription("Description");
        item.setIsAvailable(true);
        item.setOwner(owner);

        // Инициализация бронирования
        booking = new Booking();
        booking.setId(1L);
        booking.setStart(fixedTime.minusDays(2));
        booking.setEnd(fixedTime.minusDays(1));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);
    }

    // Тест создания предмета с валидными данными - должен вернуть ItemDto
    @Test
    void createItem_ValidData_ReturnsItemDto() {
        ItemCreateDto itemCreateDto = new ItemCreateDto();
        itemCreateDto.setName("Item");
        itemCreateDto.setDescription("Description");
        itemCreateDto.setAvailable(true);

        Item item = new Item();
        item.setId(1L);
        item.setName("Item");
        item.setDescription("Description");
        item.setIsAvailable(true);
        item.setOwner(owner);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.create(itemCreateDto, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Item", result.getName());
        assertEquals("Description", result.getDescription());
        assertTrue(result.getAvailable());

        verify(userRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    // Тест создания предмета для несуществующего пользователя - должен выбросить NotFoundException
    @Test
    void createItem_UserNotFound_ThrowsException() {
        ItemCreateDto itemCreateDto = new ItemCreateDto();
        itemCreateDto.setName("Item");
        itemCreateDto.setDescription("Description");
        itemCreateDto.setAvailable(true);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.create(itemCreateDto, 999L));

        verify(userRepository, times(1)).findById(999L);
        verify(itemRepository, never()).save(any(Item.class));
    }

    // Тест обновления предмета не владельцем - должен выбросить AccessDeniedException
    @Test
    void updateItem_NotOwner_ThrowsException() {
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto();
        itemUpdateDto.setName("UpdatedItem");

        User notOwner = new User();
        notOwner.setId(2L);

        Item existingItem = new Item();
        existingItem.setId(1L);
        existingItem.setOwner(owner);

        when(userRepository.findById(2L)).thenReturn(Optional.of(notOwner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(existingItem));

        assertThrows(AccessDeniedException.class, () -> itemService.update(itemUpdateDto, 1L, 2L));

        verify(userRepository, times(1)).findById(2L);
        verify(itemRepository, times(1)).findById(1L);
        verify(itemRepository, never()).save(any(Item.class));
    }

    // Тест поиска предметов по тексту - должен вернуть список найденных предметов
    @Test
    void searchItems_ValidText_ReturnsItems() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Drill");
        item.setDescription("Powerful drill");
        item.setIsAvailable(true);
        item.setOwner(owner);

        when(itemRepository.searchAvailableItems("drill")).thenReturn(List.of(item));

        List<ItemDto> results = itemService.search("drill");

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals("Drill", results.get(0).getName());

        verify(itemRepository, times(1)).searchAvailableItems("drill");
    }

    // Тест поиска с пустым текстом - должен вернуть пустой список
    @Test
    void searchItems_EmptyText_ReturnsEmptyList() {
        List<ItemDto> results = itemService.search("");

        assertTrue(results.isEmpty());

        verify(itemRepository, never()).searchAvailableItems(anyString());
    }

    // Тест получения списка предметов пользователя
    @Test
    void getList_ValidUser_ReturnsItems() {
        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("Item1");
        item1.setDescription("Description1");
        item1.setIsAvailable(true);
        item1.setOwner(owner);

        Item item2 = new Item();
        item2.setId(2L);
        item2.setName("Item2");
        item2.setDescription("Description2");
        item2.setIsAvailable(false);
        item2.setOwner(owner);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemRepository.findByOwnerIdOrderById(1L)).thenReturn(List.of(item1, item2));
        when(bookingRepository.findLastBookingForItem(anyLong(), any())).thenReturn(List.of());
        when(bookingRepository.findNextBookingForItem(anyLong(), any())).thenReturn(List.of());
        when(commentRepository.findByItemIdWithAuthor(anyLong())).thenReturn(List.of());

        List<ItemWithBookingsDto> results = itemService.getList(1L);

        assertNotNull(results);
        assertEquals(2, results.size());

        verify(userRepository, times(1)).existsById(1L);
        verify(itemRepository, times(1)).findByOwnerIdOrderById(1L);
    }

    // Тест получения предмета по ID
    @Test
    void retrieveItem_ValidId_ReturnsItem() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Item");
        item.setDescription("Description");
        item.setIsAvailable(true);
        item.setOwner(owner);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.findLastBookingForItem(anyLong(), any())).thenReturn(List.of());
        when(bookingRepository.findNextBookingForItem(anyLong(), any())).thenReturn(List.of());
        when(commentRepository.findByItemIdWithAuthor(anyLong())).thenReturn(List.of());

        ItemDetailedDto result = itemService.retrieve(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Item", result.getName());
        assertEquals("Description", result.getDescription());
        assertTrue(result.getAvailable());

        verify(itemRepository, times(1)).findById(1L);
    }

    // Тест обновления предмета владельцем
    @Test
    void updateItem_Owner_UpdatesItem() {
        Item existingItem = new Item();
        existingItem.setId(1L);
        existingItem.setName("OldName");
        existingItem.setDescription("OldDescription");
        existingItem.setIsAvailable(true);
        existingItem.setOwner(owner);

        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName("NewName");
        updateDto.setDescription("NewDescription");
        updateDto.setAvailable(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemDto result = itemService.update(updateDto, 1L, 1L);

        assertNotNull(result);
        assertEquals("NewName", result.getName());
        assertEquals("NewDescription", result.getDescription());
        assertFalse(result.getAvailable());

        verify(userRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    // Тест добавления комментария с валидными данными
    @Test
    void addComment_ValidData_ReturnsCommentDto() {
        CommentCreateDto commentCreateDto = new CommentCreateDto();
        commentCreateDto.setText("Great item!");

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Great item!");
        comment.setItem(item);
        comment.setAuthor(booker);
        comment.setCreated(fixedTime);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(eq(2L), eq(1L), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto result = itemService.addComment(1L, 2L, commentCreateDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Great item!", result.getText());

        verify(itemRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(2L);
        verify(bookingRepository, times(1)).existsByBookerIdAndItemIdAndStatusAndEndBefore(anyLong(), anyLong(), any(), any());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    // Тест добавления комментария пользователем, который не брал вещь - должен выбросить исключение
    @Test
    void addComment_UserNotBooker_ThrowsException() {
        CommentCreateDto commentCreateDto = new CommentCreateDto();
        commentCreateDto.setText("Great item!");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(eq(2L), eq(1L), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(false);

        assertThrows(ValidationException.class, () -> itemService.addComment(1L, 2L, commentCreateDto));

        verify(itemRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(2L);
        verify(bookingRepository, times(1)).existsByBookerIdAndItemIdAndStatusAndEndBefore(anyLong(), anyLong(), any(), any());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    // Тест получения несуществующего предмета - должен выбросить NotFoundException
    @Test
    void retrieveItem_ItemNotFound_ThrowsException() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.retrieve(999L, 1L));

        verify(itemRepository, times(1)).findById(999L);
    }

    // Тест получения списка предметов несуществующего пользователя - должен выбросить NotFoundException
    @Test
    void getList_UserNotFound_ThrowsException() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> itemService.getList(999L));

        verify(userRepository, times(1)).existsById(999L);
        verify(itemRepository, never()).findByOwnerIdOrderById(anyLong());
    }

    // Тест добавления комментария к несуществующей вещи
    @Test
    void addComment_ItemNotFound_ThrowsException() {
        CommentCreateDto commentCreateDto = new CommentCreateDto();
        commentCreateDto.setText("Great item!");

        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.addComment(999L, 1L, commentCreateDto));

        verify(itemRepository, times(1)).findById(999L);
        verify(userRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).existsByBookerIdAndItemIdAndStatusAndEndBefore(anyLong(), anyLong(), any(), any());
    }

    // Тест добавления комментария несуществующим пользователем
    @Test
    void addComment_UserNotFound_ThrowsException() {
        CommentCreateDto commentCreateDto = new CommentCreateDto();
        commentCreateDto.setText("Great item!");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.addComment(1L, 999L, commentCreateDto));

        verify(itemRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(999L);
        verify(bookingRepository, never()).existsByBookerIdAndItemIdAndStatusAndEndBefore(anyLong(), anyLong(), any(), any());
    }
}