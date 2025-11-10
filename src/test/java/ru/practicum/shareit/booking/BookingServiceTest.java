package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.service.impl.BookingServiceImpl;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    private BookingService bookingService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private BookingCreateDto bookingCreateDto;
    private LocalDateTime futureTime;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(bookingRepository, itemRepository, userRepository);

        // Используем будущую дату для тестов создания бронирования
        futureTime = LocalDateTime.now().plusDays(1);

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
        booking.setStart(futureTime.plusDays(1));
        booking.setEnd(futureTime.plusDays(2));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        // Инициализация DTO для создания бронирования
        bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(1L);
        bookingCreateDto.setStart(futureTime.plusDays(1));
        bookingCreateDto.setEnd(futureTime.plusDays(2));
    }

    // Тест создания бронирования с валидными данными
    @Test
    void createBooking_ValidData_ReturnsBookingDto() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto result = bookingService.create(bookingCreateDto, 2L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(BookingStatus.WAITING, result.getStatus());
        assertEquals(1L, result.getItem().getId());
        assertEquals(2L, result.getBooker().getId());

        verify(userRepository, times(1)).findById(2L);
        verify(itemRepository, times(1)).findById(1L);
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    // Тест создания бронирования для несуществующего пользователя
    @Test
    void createBooking_UserNotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.create(bookingCreateDto, 999L));

        verify(userRepository, times(1)).findById(999L);
        verify(itemRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    // Тест создания бронирования для несуществующей вещи
    @Test
    void createBooking_ItemNotFound_ThrowsException() {
        BookingCreateDto nonExistentItemDto = new BookingCreateDto();
        nonExistentItemDto.setItemId(999L);
        nonExistentItemDto.setStart(futureTime.plusDays(1));
        nonExistentItemDto.setEnd(futureTime.plusDays(2));

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.create(nonExistentItemDto, 2L));

        verify(userRepository, times(1)).findById(2L);
        verify(itemRepository, times(1)).findById(999L);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    // Тест создания бронирования для недоступной вещи
    @Test
    void createBooking_ItemNotAvailable_ThrowsException() {
        item.setIsAvailable(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () -> bookingService.create(bookingCreateDto, 2L));

        verify(userRepository, times(1)).findById(2L);
        verify(itemRepository, times(1)).findById(1L);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    // Тест создания бронирования владельцем своей вещи
    @Test
    void createBooking_OwnerBookingOwnItem_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> bookingService.create(bookingCreateDto, 1L));

        verify(userRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).findById(1L);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    // Тест создания бронирования с некорректными датами (конец раньше начала)
    @Test
    void createBooking_InvalidDates_ThrowsException() {
        bookingCreateDto.setStart(futureTime.plusDays(2));
        bookingCreateDto.setEnd(futureTime.plusDays(1)); // End before start

        // Валидация дат происходит до обращения к репозиториям
        assertThrows(ValidationException.class, () -> bookingService.create(bookingCreateDto, 2L));

        // Проверяем, что репозитории не вызывались
        verify(userRepository, never()).findById(anyLong());
        verify(itemRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    // Тест подтверждения бронирования
    @Test
    void approveBooking_ValidApproval_ReturnsApprovedBooking() {
        when(bookingRepository.findByIdWithItemAndBooker(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto result = bookingService.approve(1L, 1L, true);

        assertNotNull(result);
        assertEquals(BookingStatus.APPROVED, result.getStatus());

        verify(bookingRepository, times(1)).findByIdWithItemAndBooker(1L);
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    // Тест отклонения бронирования
    @Test
    void approveBooking_ValidRejection_ReturnsRejectedBooking() {
        when(bookingRepository.findByIdWithItemAndBooker(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto result = bookingService.approve(1L, 1L, false);

        assertNotNull(result);
        assertEquals(BookingStatus.REJECTED, result.getStatus());

        verify(bookingRepository, times(1)).findByIdWithItemAndBooker(1L);
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    // Тест подтверждения бронирования не владельцем
    @Test
    void approveBooking_NotOwner_ThrowsException() {
        when(bookingRepository.findByIdWithItemAndBooker(1L)).thenReturn(Optional.of(booking));

        assertThrows(AccessDeniedException.class, () -> bookingService.approve(1L, 2L, true));

        verify(bookingRepository, times(1)).findByIdWithItemAndBooker(1L);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    // Тест подтверждения уже обработанного бронирования
    @Test
    void approveBooking_AlreadyProcessed_ThrowsException() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findByIdWithItemAndBooker(1L)).thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class, () -> bookingService.approve(1L, 1L, true));

        verify(bookingRepository, times(1)).findByIdWithItemAndBooker(1L);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    // Тест получения бронирования по ID
    @Test
    void getBooking_ValidIds_ReturnsBooking() {
        when(bookingRepository.findByIdWithItemAndBooker(1L)).thenReturn(Optional.of(booking));

        BookingDto result = bookingService.getById(1L, 1L); // Owner accessing

        assertNotNull(result);
        assertEquals(1L, result.getId());

        verify(bookingRepository, times(1)).findByIdWithItemAndBooker(1L);
    }

    // Тест получения бронирования пользователем без доступа
    @Test
    void getBooking_NoAccess_ThrowsException() {
        User otherUser = new User();
        otherUser.setId(3L);

        when(bookingRepository.findByIdWithItemAndBooker(1L)).thenReturn(Optional.of(booking));

        assertThrows(AccessDeniedException.class, () -> bookingService.getById(1L, 3L));

        verify(bookingRepository, times(1)).findByIdWithItemAndBooker(1L);
    }

    // Тест получения несуществующего бронирования
    @Test
    void getBooking_NotFound_ThrowsException() {
        when(bookingRepository.findByIdWithItemAndBooker(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.getById(999L, 1L));

        verify(bookingRepository, times(1)).findByIdWithItemAndBooker(999L);
    }

    // Тест получения всех бронирований пользователя
    @Test
    void getUserBookings_AllState_ReturnsBookings() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(bookingRepository.findByBookerId(eq(2L), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getUserBookings(2L, "ALL");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());

        verify(userRepository, times(1)).existsById(2L);
        verify(bookingRepository, times(1)).findByBookerId(eq(2L), any(Sort.class));
    }

    // Тест получения текущих бронирований пользователя
    @Test
    void getUserBookings_CurrentState_ReturnsBookings() {
        // Создаем бронирование, которое сейчас активно
        Booking currentBooking = new Booking();
        currentBooking.setId(2L);
        currentBooking.setStart(LocalDateTime.now().minusDays(1));
        currentBooking.setEnd(LocalDateTime.now().plusDays(1));
        currentBooking.setItem(item);
        currentBooking.setBooker(booker);
        currentBooking.setStatus(BookingStatus.APPROVED);

        when(userRepository.existsById(2L)).thenReturn(true);
        when(bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(eq(2L), any(LocalDateTime.class), any(LocalDateTime.class), any(Sort.class)))
                .thenReturn(List.of(currentBooking));

        List<BookingDto> result = bookingService.getUserBookings(2L, "CURRENT");

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(userRepository, times(1)).existsById(2L);
        verify(bookingRepository, times(1)).findByBookerIdAndStartBeforeAndEndAfter(eq(2L), any(LocalDateTime.class), any(LocalDateTime.class), any(Sort.class));
    }

    // Тест получения завершенных бронирований пользователя
    @Test
    void getUserBookings_PastState_ReturnsBookings() {
        // Создаем завершенное бронирование
        Booking pastBooking = new Booking();
        pastBooking.setId(3L);
        pastBooking.setStart(LocalDateTime.now().minusDays(3));
        pastBooking.setEnd(LocalDateTime.now().minusDays(1));
        pastBooking.setItem(item);
        pastBooking.setBooker(booker);
        pastBooking.setStatus(BookingStatus.APPROVED);

        when(userRepository.existsById(2L)).thenReturn(true);
        when(bookingRepository.findByBookerIdAndEndBefore(eq(2L), any(LocalDateTime.class), any(Sort.class)))
                .thenReturn(List.of(pastBooking));

        List<BookingDto> result = bookingService.getUserBookings(2L, "PAST");

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(userRepository, times(1)).existsById(2L);
        verify(bookingRepository, times(1)).findByBookerIdAndEndBefore(eq(2L), any(LocalDateTime.class), any(Sort.class));
    }

    // Тест получения будущих бронирований пользователя
    @Test
    void getUserBookings_FutureState_ReturnsBookings() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(bookingRepository.findByBookerIdAndStartAfter(eq(2L), any(LocalDateTime.class), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getUserBookings(2L, "FUTURE");

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(userRepository, times(1)).existsById(2L);
        verify(bookingRepository, times(1)).findByBookerIdAndStartAfter(eq(2L), any(LocalDateTime.class), any(Sort.class));
    }

    // Тест получения ожидающих бронирований пользователя
    @Test
    void getUserBookings_WaitingState_ReturnsBookings() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(bookingRepository.findByBookerIdAndStatus(eq(2L), eq(BookingStatus.WAITING), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getUserBookings(2L, "WAITING");

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(userRepository, times(1)).existsById(2L);
        verify(bookingRepository, times(1)).findByBookerIdAndStatus(eq(2L), eq(BookingStatus.WAITING), any(Sort.class));
    }

    // Тест получения отклоненных бронирований пользователя
    @Test
    void getUserBookings_RejectedState_ReturnsBookings() {
        Booking rejectedBooking = new Booking();
        rejectedBooking.setId(4L);
        rejectedBooking.setStart(futureTime.plusDays(1));
        rejectedBooking.setEnd(futureTime.plusDays(2));
        rejectedBooking.setItem(item);
        rejectedBooking.setBooker(booker);
        rejectedBooking.setStatus(BookingStatus.REJECTED);

        when(userRepository.existsById(2L)).thenReturn(true);
        when(bookingRepository.findByBookerIdAndStatus(eq(2L), eq(BookingStatus.REJECTED), any(Sort.class)))
                .thenReturn(List.of(rejectedBooking));

        List<BookingDto> result = bookingService.getUserBookings(2L, "REJECTED");

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(userRepository, times(1)).existsById(2L);
        verify(bookingRepository, times(1)).findByBookerIdAndStatus(eq(2L), eq(BookingStatus.REJECTED), any(Sort.class));
    }

    // Тест получения бронирований для несуществующего пользователя
    @Test
    void getUserBookings_UserNotFound_ThrowsException() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> bookingService.getUserBookings(999L, "ALL"));

        verify(userRepository, times(1)).existsById(999L);
        verify(bookingRepository, never()).findByBookerId(anyLong(), any(Sort.class));
    }

    // Тест получения бронирований с неизвестным состоянием
    @Test
    void getUserBookings_UnknownState_ThrowsException() {
        when(userRepository.existsById(2L)).thenReturn(true);

        assertThrows(ValidationException.class, () -> bookingService.getUserBookings(2L, "UNKNOWN"));

        verify(userRepository, times(1)).existsById(2L);
        verify(bookingRepository, never()).findByBookerId(anyLong(), any(Sort.class));
    }

    // Тест получения бронирований для вещей владельца
    @Test
    void getOwnerBookings_AllState_ReturnsBookings() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findByItemOwnerId(eq(1L), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getOwnerBookings(1L, "ALL");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());

        verify(userRepository, times(1)).existsById(1L);
        verify(bookingRepository, times(1)).findByItemOwnerId(eq(1L), any(Sort.class));
    }

    // Тест получения бронирований для вещей несуществующего владельца
    @Test
    void getOwnerBookings_UserNotFound_ThrowsException() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> bookingService.getOwnerBookings(999L, "ALL"));

        verify(userRepository, times(1)).existsById(999L);
        verify(bookingRepository, never()).findByItemOwnerId(anyLong(), any(Sort.class));
    }

    // Тест получения бронирования автором бронирования
    @Test
    void getBooking_ByBooker_ReturnsBooking() {
        when(bookingRepository.findByIdWithItemAndBooker(1L)).thenReturn(Optional.of(booking));

        BookingDto result = bookingService.getById(1L, 2L); // Booker accessing

        assertNotNull(result);
        assertEquals(1L, result.getId());

        verify(bookingRepository, times(1)).findByIdWithItemAndBooker(1L);
    }

    // Тест создания бронирования с одинаковыми датами начала и окончания
    @Test
    void createBooking_SameStartAndEnd_ThrowsException() {
        bookingCreateDto.setStart(futureTime.plusDays(1));
        bookingCreateDto.setEnd(futureTime.plusDays(1));

        // Валидация дат происходит до обращения к репозиториям
        assertThrows(ValidationException.class, () -> bookingService.create(bookingCreateDto, 2L));

        // Проверяем, что репозитории не вызывались
        verify(userRepository, never()).findById(anyLong());
        verify(itemRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    // Тест получения бронирований владельца с состоянием CURRENT
    @Test
    void getOwnerBookings_CurrentState_ReturnsBookings() {
        // Создаем текущее бронирование для владельца
        Booking currentBooking = new Booking();
        currentBooking.setId(5L);
        currentBooking.setStart(LocalDateTime.now().minusDays(1));
        currentBooking.setEnd(LocalDateTime.now().plusDays(1));
        currentBooking.setItem(item);
        currentBooking.setBooker(booker);
        currentBooking.setStatus(BookingStatus.APPROVED);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfter(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), any(Sort.class)))
                .thenReturn(List.of(currentBooking));

        List<BookingDto> result = bookingService.getOwnerBookings(1L, "CURRENT");

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(userRepository, times(1)).existsById(1L);
        verify(bookingRepository, times(1)).findByItemOwnerIdAndStartBeforeAndEndAfter(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), any(Sort.class));
    }

    // Тест создания бронирования с датами в прошлом - исправленный тест
    @Test
    void createBooking_PastDates_ThrowsException() {
        BookingCreateDto pastBooking = new BookingCreateDto();
        pastBooking.setItemId(1L);
        pastBooking.setStart(LocalDateTime.now().minusDays(2)); // Прошлая дата
        pastBooking.setEnd(LocalDateTime.now().minusDays(1));   // Прошлая дата

        // Валидация дат происходит до обращения к репозиториям
        assertThrows(ValidationException.class, () -> bookingService.create(pastBooking, 2L));

        // Проверяем, что репозитории не вызывались
        verify(userRepository, never()).findById(anyLong());
        verify(itemRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    // Тест создания бронирования с датой начала в прошлом - исправленный тест
    @Test
    void createBooking_PastStartDate_ThrowsException() {
        BookingCreateDto pastStartBooking = new BookingCreateDto();
        pastStartBooking.setItemId(1L);
        pastStartBooking.setStart(LocalDateTime.now().minusDays(1)); // Прошлая дата
        pastStartBooking.setEnd(LocalDateTime.now().plusDays(1));    // Будущая дата

        // Валидация дат происходит до обращения к репозиториям
        assertThrows(ValidationException.class, () -> bookingService.create(pastStartBooking, 2L));

        // Проверяем, что репозитории не вызывались
        verify(userRepository, never()).findById(anyLong());
        verify(itemRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    // Тест создания бронирования с датой окончания в прошлом - исправленный тест
    @Test
    void createBooking_PastEndDate_ThrowsException() {
        BookingCreateDto pastEndBooking = new BookingCreateDto();
        pastEndBooking.setItemId(1L);
        pastEndBooking.setStart(LocalDateTime.now().plusDays(1));    // Будущая дата
        pastEndBooking.setEnd(LocalDateTime.now().minusDays(1));     // Прошлая дата

        // Валидация дат происходит до обращения к репозиториям
        assertThrows(ValidationException.class, () -> bookingService.create(pastEndBooking, 2L));

        // Проверяем, что репозитории не вызывались
        verify(userRepository, never()).findById(anyLong());
        verify(itemRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    // Тест отмены бронирования
    @Test
    void cancelBooking_ValidCancellation_ReturnsCanceledBooking() {
        when(bookingRepository.findByIdWithItemAndBooker(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto result = bookingService.cancel(1L, 2L); // Booker canceling

        assertNotNull(result);
        assertEquals(BookingStatus.CANCELED, result.getStatus());

        verify(bookingRepository, times(1)).findByIdWithItemAndBooker(1L);
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    // Тест отмены бронирования не автором
    @Test
    void cancelBooking_NotBooker_ThrowsException() {
        when(bookingRepository.findByIdWithItemAndBooker(1L)).thenReturn(Optional.of(booking));

        assertThrows(AccessDeniedException.class, () -> bookingService.cancel(1L, 3L)); // Not the booker

        verify(bookingRepository, times(1)).findByIdWithItemAndBooker(1L);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    // Тест отмены уже обработанного бронирования
    @Test
    void cancelBooking_AlreadyProcessed_ThrowsException() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findByIdWithItemAndBooker(1L)).thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class, () -> bookingService.cancel(1L, 2L));

        verify(bookingRepository, times(1)).findByIdWithItemAndBooker(1L);
        verify(bookingRepository, never()).save(any(Booking.class));
    }
}