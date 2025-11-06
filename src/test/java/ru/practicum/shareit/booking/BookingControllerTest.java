package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingCreateDto bookingCreateDto;
    private BookingDto bookingDto;
    private BookingDto approvedBookingDto;
    private UserDto bookerDto;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        // Инициализация UserDto
        bookerDto = new UserDto();
        bookerDto.setId(2L);
        bookerDto.setName("Booker");
        bookerDto.setEmail("booker@example.com");

        // Инициализация ItemDto
        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Item");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);

        // Инициализация BookingCreateDto
        bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(1L);
        bookingCreateDto.setStart(LocalDateTime.now().plusDays(1));
        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(2));

        // Инициализация BookingDto
        bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        bookingDto.setStatus(BookingStatus.WAITING);
        bookingDto.setItem(itemDto);
        bookingDto.setBooker(bookerDto);

        // Инициализация approved BookingDto
        approvedBookingDto = new BookingDto();
        approvedBookingDto.setId(1L);
        approvedBookingDto.setStart(LocalDateTime.now().plusDays(1));
        approvedBookingDto.setEnd(LocalDateTime.now().plusDays(2));
        approvedBookingDto.setStatus(BookingStatus.APPROVED);
        approvedBookingDto.setItem(itemDto);
        approvedBookingDto.setBooker(bookerDto);
    }

    // Тест создания бронирования с валидными данными
    @Test
    void createBooking_ValidData_ReturnsBookingDto() throws Exception {
        Mockito.when(bookingService.create(any(), anyLong()))
                .thenReturn(bookingDto);

        mockMvc.perform(post("/bookings")
                        .header(BookingController.USER_HEADER, "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("WAITING")))
                .andExpect(jsonPath("$.item.id", is(1)))
                .andExpect(jsonPath("$.booker.id", is(2)));
    }

    // Тест создания бронирования без заголовка пользователя
    @Test
    void createBooking_MissingUserId_ReturnsError() throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isInternalServerError());
    }

    // Тест создания бронирования с невалидными датами
    @Test
    void createBooking_InvalidDates_ReturnsBadRequest() throws Exception {
        BookingCreateDto invalidBooking = new BookingCreateDto();
        invalidBooking.setItemId(1L);
        invalidBooking.setStart(LocalDateTime.now().plusDays(2));
        invalidBooking.setEnd(LocalDateTime.now().plusDays(1)); // End before start

        Mockito.when(bookingService.create(any(), anyLong()))
                .thenThrow(new ValidationException("Дата начала должна быть раньше даты окончания"));

        mockMvc.perform(post("/bookings")
                        .header(BookingController.USER_HEADER, "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBooking)))
                .andExpect(status().isBadRequest());
    }

    // Тест создания бронирования для недоступной вещи
    @Test
    void createBooking_ItemNotAvailable_ReturnsBadRequest() throws Exception {
        Mockito.when(bookingService.create(any(), anyLong()))
                .thenThrow(new ValidationException("Вещь недоступна для бронирования"));

        mockMvc.perform(post("/bookings")
                        .header(BookingController.USER_HEADER, "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isBadRequest());
    }

    // Тест создания бронирования владельцем своей вещи
    @Test
    void createBooking_OwnerBookingOwnItem_ReturnsNotFound() throws Exception {
        Mockito.when(bookingService.create(any(), anyLong()))
                .thenThrow(new NotFoundException("Владелец не может бронировать свою вещь"));

        mockMvc.perform(post("/bookings")
                        .header(BookingController.USER_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isNotFound());
    }

    // Тест подтверждения бронирования
    @Test
    void approveBooking_ValidApproval_ReturnsApprovedBooking() throws Exception {
        Mockito.when(bookingService.approve(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(approvedBookingDto);

        mockMvc.perform(patch("/bookings/1")
                        .header(BookingController.USER_HEADER, "1")
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }

    // Тест отклонения бронирования
    @Test
    void approveBooking_ValidRejection_ReturnsRejectedBooking() throws Exception {
        BookingDto rejectedBooking = new BookingDto();
        rejectedBooking.setId(1L);
        rejectedBooking.setStatus(BookingStatus.REJECTED);

        Mockito.when(bookingService.approve(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(rejectedBooking);

        mockMvc.perform(patch("/bookings/1")
                        .header(BookingController.USER_HEADER, "1")
                        .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("REJECTED")));
    }

    // Тест подтверждения бронирования не владельцем
    @Test
    void approveBooking_NotOwner_ReturnsForbidden() throws Exception {
        Mockito.when(bookingService.approve(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new AccessDeniedException("Только владелец вещи может подтверждать бронирование"));

        mockMvc.perform(patch("/bookings/1")
                        .header(BookingController.USER_HEADER, "2")
                        .param("approved", "true"))
                .andExpect(status().isForbidden());
    }

    // Тест получения бронирования по ID
    @Test
    void getBooking_ValidIds_ReturnsBooking() throws Exception {
        Mockito.when(bookingService.getById(anyLong(), anyLong()))
                .thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/1")
                        .header(BookingController.USER_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("WAITING")));
    }

    // Тест получения бронирования пользователем без доступа
    @Test
    void getBooking_NoAccess_ReturnsForbidden() throws Exception {
        Mockito.when(bookingService.getById(anyLong(), anyLong()))
                .thenThrow(new AccessDeniedException("Доступ к бронированию запрещен"));

        mockMvc.perform(get("/bookings/1")
                        .header(BookingController.USER_HEADER, "3"))
                .andExpect(status().isForbidden());
    }

    // Тест получения несуществующего бронирования
    @Test
    void getBooking_NotFound_ReturnsNotFound() throws Exception {
        Mockito.when(bookingService.getById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Бронирование с id=999 не найдено"));

        mockMvc.perform(get("/bookings/999")
                        .header(BookingController.USER_HEADER, "1"))
                .andExpect(status().isNotFound());
    }

    // Тест получения всех бронирований пользователя
    @Test
    void getUserBookings_ValidUser_ReturnsBookings() throws Exception {
        Mockito.when(bookingService.getUserBookings(anyLong(), any()))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .header(BookingController.USER_HEADER, "2")
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].booker.id", is(2)));
    }

    // Тест получения бронирований пользователя с разными состояниями
    @Test
    void getUserBookings_DifferentStates_ReturnsBookings() throws Exception {
        Mockito.when(bookingService.getUserBookings(anyLong(), any()))
                .thenReturn(List.of(bookingDto));

        // Test ALL state
        mockMvc.perform(get("/bookings")
                        .header(BookingController.USER_HEADER, "2")
                        .param("state", "ALL"))
                .andExpect(status().isOk());

        // Test CURRENT state
        mockMvc.perform(get("/bookings")
                        .header(BookingController.USER_HEADER, "2")
                        .param("state", "CURRENT"))
                .andExpect(status().isOk());

        // Test FUTURE state
        mockMvc.perform(get("/bookings")
                        .header(BookingController.USER_HEADER, "2")
                        .param("state", "FUTURE"))
                .andExpect(status().isOk());

        // Test WAITING state
        mockMvc.perform(get("/bookings")
                        .header(BookingController.USER_HEADER, "2")
                        .param("state", "WAITING"))
                .andExpect(status().isOk());
    }

    // Тест получения бронирований пользователя с неизвестным состоянием
    @Test
    void getUserBookings_UnknownState_ReturnsBadRequest() throws Exception {
        Mockito.when(bookingService.getUserBookings(anyLong(), any()))
                .thenThrow(new ValidationException("Unknown state: UNKNOWN"));

        mockMvc.perform(get("/bookings")
                        .header(BookingController.USER_HEADER, "2")
                        .param("state", "UNKNOWN"))
                .andExpect(status().isBadRequest());
    }

    // Тест получения бронирований для вещей владельца
    @Test
    void getOwnerBookings_ValidOwner_ReturnsBookings() throws Exception {
        Mockito.when(bookingService.getOwnerBookings(anyLong(), any()))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings/owner")
                        .header(BookingController.USER_HEADER, "1")
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].item.id", is(1)));
    }

    // Тест получения бронирований для несуществующего пользователя
    @Test
    void getOwnerBookings_UserNotFound_ReturnsNotFound() throws Exception {
        Mockito.when(bookingService.getOwnerBookings(anyLong(), any()))
                .thenThrow(new NotFoundException("Пользователь с id=999 не найден"));

        mockMvc.perform(get("/bookings/owner")
                        .header(BookingController.USER_HEADER, "999")
                        .param("state", "ALL"))
                .andExpect(status().isNotFound());
    }

    // Тест получения бронирований с дефолтным состоянием
    @Test
    void getUserBookings_DefaultState_ReturnsBookings() throws Exception {
        Mockito.when(bookingService.getUserBookings(anyLong(), eq("ALL")))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .header(BookingController.USER_HEADER, "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)));
    }

    // Тест создания бронирования с прошедшими датами
    @Test
    void createBooking_PastDates_ReturnsBadRequest() throws Exception {
        BookingCreateDto pastBooking = new BookingCreateDto();
        pastBooking.setItemId(1L);
        pastBooking.setStart(LocalDateTime.now().minusDays(2));
        pastBooking.setEnd(LocalDateTime.now().minusDays(1));

        Mockito.when(bookingService.create(any(), anyLong()))
                .thenThrow(new ValidationException("Дата начала должна быть в будущем"));

        mockMvc.perform(post("/bookings")
                        .header(BookingController.USER_HEADER, "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pastBooking)))
                .andExpect(status().isBadRequest());
    }

    // Тест подтверждения уже обработанного бронирования
    @Test
    void approveBooking_AlreadyProcessed_ReturnsBadRequest() throws Exception {
        Mockito.when(bookingService.approve(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new ValidationException("Бронирование уже обработано"));

        mockMvc.perform(patch("/bookings/1")
                        .header(BookingController.USER_HEADER, "1")
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());
    }
}