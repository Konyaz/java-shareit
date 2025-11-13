package ru.practicum.shareit.booking.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dao.BookingMapper;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dao.Status;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User owner;
    private User booker;
    private Item item;
    private BookingCreateDto bookingCreateDto;
    private Booking booking;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setName("Owner");
        owner.setEmail("owner@example.com");

        booker = new User();
        booker.setId(2L);
        booker.setName("Booker");
        booker.setEmail("booker@example.com");

        item = new Item();
        item.setId(1L);
        item.setName("Item");
        item.setDescription("Description");
        item.setIsAvailable(true);
        item.setOwner(owner);

        bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(1L);
        bookingCreateDto.setStart(LocalDateTime.now().plusDays(1));
        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(2));

        booking = new Booking();
        booking.setId(1L);
        booking.setStart(bookingCreateDto.getStart());
        booking.setEnd(bookingCreateDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Status.WAITING);

        bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setStart(bookingCreateDto.getStart());
        bookingDto.setEnd(bookingCreateDto.getEnd());
        bookingDto.setStatus(Status.WAITING);
    }

    // Тест успешного создания бронирования с валидными данными
    @Test
    void create_ValidData_ReturnsBookingDto() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toBookingDto(any(Booking.class))).thenReturn(bookingDto);

        BookingDto result = bookingService.create(2L, bookingCreateDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(Status.WAITING, result.getStatus());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    // Тест создания бронирования с несуществующим пользователем
    @Test
    void create_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                bookingService.create(999L, bookingCreateDto));
    }

    // Тест создания бронирования с несуществующей вещью
    @Test
    void create_ItemNotFound_ThrowsNotFoundException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        bookingCreateDto.setItemId(999L);

        assertThrows(NotFoundException.class, () ->
                bookingService.create(2L, bookingCreateDto));
    }

    // Тест создания бронирования для недоступной вещи
    @Test
    void create_ItemNotAvailable_ThrowsValidationException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        item.setIsAvailable(false);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () ->
                bookingService.create(2L, bookingCreateDto));
    }

    // Тест создания бронирования владельцем своей собственной вещи
    @Test
    void create_BookOwnItem_ThrowsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () ->
                bookingService.create(1L, bookingCreateDto));
    }

    // Тест создания бронирования с некорректными датами (конец раньше начала)
    @Test
    void create_InvalidDates_ThrowsValidationException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        bookingCreateDto.setStart(LocalDateTime.now().plusDays(2));
        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(1));

        assertThrows(ValidationException.class, () ->
                bookingService.create(2L, bookingCreateDto));
    }

    // Тест создания бронирования с одинаковыми датами начала и конца
    @Test
    void create_SameStartAndEnd_ThrowsValidationException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        LocalDateTime sameTime = LocalDateTime.now().plusDays(1);
        bookingCreateDto.setStart(sameTime);
        bookingCreateDto.setEnd(sameTime);

        assertThrows(ValidationException.class, () ->
                bookingService.create(2L, bookingCreateDto));
    }

    // Тест создания бронирования с датами в прошлом
    @Test
    void create_PastDates_ThrowsValidationException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        bookingCreateDto.setStart(LocalDateTime.now().minusDays(2));
        bookingCreateDto.setEnd(LocalDateTime.now().minusDays(1));

        assertThrows(ValidationException.class, () ->
                bookingService.create(2L, bookingCreateDto));
    }

    // Тест успешного подтверждения бронирования
    @Test
    void approve_ValidApproval_ReturnsApprovedBooking() {
        BookingDto approvedBookingDto = new BookingDto();
        approvedBookingDto.setId(1L);
        approvedBookingDto.setStatus(Status.APPROVED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingDto(any(Booking.class))).thenReturn(approvedBookingDto);

        BookingDto result = bookingService.approve(1L, 1L, true);

        assertNotNull(result);
        assertEquals(Status.APPROVED, result.getStatus());
    }

    // Тест успешного отклонения бронирования
    @Test
    void approve_ValidRejection_ReturnsRejectedBooking() {
        BookingDto rejectedBookingDto = new BookingDto();
        rejectedBookingDto.setId(1L);
        rejectedBookingDto.setStatus(Status.REJECTED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingDto(any(Booking.class))).thenReturn(rejectedBookingDto);

        BookingDto result = bookingService.approve(1L, 1L, false);

        assertNotNull(result);
        assertEquals(Status.REJECTED, result.getStatus());
    }

    // Тест подтверждения бронирования не владельцем вещи
    @Test
    void approve_NotOwner_ThrowsAccessDeniedException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(AccessDeniedException.class, () ->
                bookingService.approve(999L, 1L, true));
    }

    // Тест подтверждения уже обработанного бронирования
    @Test
    void approve_AlreadyProcessed_ThrowsValidationException() {
        booking.setStatus(Status.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class, () ->
                bookingService.approve(1L, 1L, true));
    }

    // Тест успешного получения информации о бронировании
    @Test
    void get_ValidRequest_ReturnsBookingDto() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingDto(any(Booking.class))).thenReturn(bookingDto);

        BookingDto result = bookingService.get(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    // Тест получения информации о бронировании пользователем без доступа
    @Test
    void get_NoAccess_ThrowsAccessDeniedException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(AccessDeniedException.class, () ->
                bookingService.get(999L, 1L));
    }

    // Тест получения информации о несуществующем бронировании
    @Test
    void get_BookingNotFound_ThrowsNotFoundException() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                bookingService.get(1L, 999L));
    }

    // Тест получения всех бронирований пользователя с отрицательным параметром from
    @Test
    void getAllByUser_NegativeFrom_ThrowsValidationException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));

        assertThrows(ValidationException.class, () ->
                bookingService.getAllByUser(2L, "ALL", -1, 10));
    }

    // Тест получения всех бронирований пользователя с нулевым размером страницы
    @Test
    void getAllByUser_ZeroSize_ThrowsValidationException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));

        assertThrows(ValidationException.class, () ->
                bookingService.getAllByUser(2L, "ALL", 0, 0));
    }

    // Тест получения всех бронирований владельца с отрицательным параметром from
    @Test
    void getAllByOwner_NegativeFrom_ThrowsValidationException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        assertThrows(ValidationException.class, () ->
                bookingService.getAllByOwner(1L, "ALL", -1, 10));
    }

    // Тест получения всех бронирований владельца с нулевым размером страницы
    @Test
    void getAllByOwner_ZeroSize_ThrowsValidationException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        assertThrows(ValidationException.class, () ->
                bookingService.getAllByOwner(1L, "ALL", 0, 0));
    }
}