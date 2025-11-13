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
import ru.practicum.shareit.booking.dao.Status;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
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
import static ru.practicum.shareit.Constant.OWNER_HEADER;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingCreateDto validBookingCreateDto;
    private BookingDto bookingDto;
    private BookingDto approvedBookingDto;
    private ItemDto itemDto;
    private UserDto userDto;
    private UserDto bookerDto;

    @BeforeEach
    void init() {
        validBookingCreateDto = new BookingCreateDto();
        validBookingCreateDto.setStart(LocalDateTime.now().plusDays(1));
        validBookingCreateDto.setEnd(LocalDateTime.now().plusDays(2));
        validBookingCreateDto.setItemId(1L);

        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Item");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("Owner");
        userDto.setEmail("owner@example.com");

        bookerDto = new UserDto();
        bookerDto.setId(2L);
        bookerDto.setName("Booker");
        bookerDto.setEmail("booker@example.com");

        bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setStart(validBookingCreateDto.getStart());
        bookingDto.setEnd(validBookingCreateDto.getEnd());
        bookingDto.setItem(itemDto);
        bookingDto.setBooker(bookerDto);
        bookingDto.setStatus(Status.WAITING);

        approvedBookingDto = new BookingDto();
        approvedBookingDto.setId(1L);
        approvedBookingDto.setStart(validBookingCreateDto.getStart());
        approvedBookingDto.setEnd(validBookingCreateDto.getEnd());
        approvedBookingDto.setItem(itemDto);
        approvedBookingDto.setBooker(bookerDto);
        approvedBookingDto.setStatus(Status.APPROVED);
    }

    // Создание бронирования с валидными данными
    @Test
    void createBooking_ValidData_ReturnsBookingDto() throws Exception {
        Mockito.when(bookingService.create(anyLong(), any(BookingCreateDto.class))).thenReturn(bookingDto);

        mockMvc.perform(post("/bookings")
                        .header(OWNER_HEADER, "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBookingCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.start").exists())
                .andExpect(jsonPath("$.end").exists())
                .andExpect(jsonPath("$.item.id", is(1)))
                .andExpect(jsonPath("$.booker.id", is(2)))
                .andExpect(jsonPath("$.status", is("WAITING")));
    }

    // Создание бронирования без заголовка пользователя
    @Test
    void createBooking_MissingUserIdHeader_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBookingCreateDto)))
                .andExpect(status().isBadRequest());
    }

    // Создание бронирования для несуществующего предмета
    @Test
    void createBooking_ItemNotFound_ReturnsNotFound() throws Exception {
        Mockito.when(bookingService.create(anyLong(), any(BookingCreateDto.class)))
                .thenThrow(new NotFoundException("Вещь с id=999 не найдена"));

        mockMvc.perform(post("/bookings")
                        .header(OWNER_HEADER, "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBookingCreateDto)))
                .andExpect(status().isNotFound());
    }

    // Создание бронирования с ValidationException от сервиса
    @Test
    void createBooking_ValidationException_ReturnsBadRequest() throws Exception {
        Mockito.when(bookingService.create(anyLong(), any(BookingCreateDto.class)))
                .thenThrow(new ValidationException("Неверные даты бронирования"));

        mockMvc.perform(post("/bookings")
                        .header(OWNER_HEADER, "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBookingCreateDto)))
                .andExpect(status().isBadRequest());
    }

    // Подтверждение бронирования с валидными данными
    @Test
    void approveBooking_ValidData_ReturnsApprovedBooking() throws Exception {
        Mockito.when(bookingService.approve(anyLong(), anyLong(), anyBoolean())).thenReturn(approvedBookingDto);

        mockMvc.perform(patch("/bookings/1")
                        .header(OWNER_HEADER, "1")
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }

    // Подтверждение бронирования с отклонением
    @Test
    void approveBooking_RejectBooking_ReturnsRejectedBooking() throws Exception {
        BookingDto rejectedBookingDto = new BookingDto();
        rejectedBookingDto.setId(1L);
        rejectedBookingDto.setStatus(Status.REJECTED);

        Mockito.when(bookingService.approve(anyLong(), anyLong(), anyBoolean())).thenReturn(rejectedBookingDto);

        mockMvc.perform(patch("/bookings/1")
                        .header(OWNER_HEADER, "1")
                        .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("REJECTED")));
    }

    // Подтверждение бронирования без заголовка пользователя
    @Test
    void approveBooking_MissingUserIdHeader_ReturnsBadRequest() throws Exception {
        mockMvc.perform(patch("/bookings/1")
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());
    }

    // Подтверждение бронирования без параметра approved
    @Test
    void approveBooking_MissingApprovedParam_ReturnsBadRequest() throws Exception {
        mockMvc.perform(patch("/bookings/1")
                        .header(OWNER_HEADER, "1"))
                .andExpect(status().isBadRequest());
    }

    // Подтверждение бронирования без прав доступа
    @Test
    void approveBooking_AccessDenied_ReturnsForbidden() throws Exception {
        Mockito.when(bookingService.approve(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new AccessDeniedException("Пользователь не является владельцем вещи"));

        mockMvc.perform(patch("/bookings/1")
                        .header(OWNER_HEADER, "2")
                        .param("approved", "true"))
                .andExpect(status().isForbidden());
    }

    // Подтверждение несуществующего бронирования
    @Test
    void approveBooking_NotFound_ReturnsNotFound() throws Exception {
        Mockito.when(bookingService.approve(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new NotFoundException("Бронирование с id=999 не найдено"));

        mockMvc.perform(patch("/bookings/999")
                        .header(OWNER_HEADER, "1")
                        .param("approved", "true"))
                .andExpect(status().isNotFound());
    }

    // Подтверждение бронирования с ValidationException
    @Test
    void approveBooking_ValidationException_ReturnsBadRequest() throws Exception {
        Mockito.when(bookingService.approve(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new ValidationException("Бронирование уже обработано"));

        mockMvc.perform(patch("/bookings/1")
                        .header(OWNER_HEADER, "1")
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());
    }

    // Получение бронирования по ID
    @Test
    void getBooking_ValidData_ReturnsBooking() throws Exception {
        Mockito.when(bookingService.get(anyLong(), anyLong())).thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/1")
                        .header(OWNER_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.item.id", is(1)))
                .andExpect(jsonPath("$.booker.id", is(2)))
                .andExpect(jsonPath("$.status", is("WAITING")));
    }

    // Получение бронирования без заголовка пользователя
    @Test
    void getBooking_MissingUserIdHeader_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/1"))
                .andExpect(status().isBadRequest());
    }

    // Получение несуществующего бронирования
    @Test
    void getBooking_NotFound_ReturnsNotFound() throws Exception {
        Mockito.when(bookingService.get(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Бронирование с id=999 не найдено"));

        mockMvc.perform(get("/bookings/999")
                        .header(OWNER_HEADER, "1"))
                .andExpect(status().isNotFound());
    }

    // Получение бронирования без прав доступа
    @Test
    void getBooking_AccessDenied_ReturnsForbidden() throws Exception {
        Mockito.when(bookingService.get(anyLong(), anyLong()))
                .thenThrow(new AccessDeniedException("Доступ запрещен"));

        mockMvc.perform(get("/bookings/1")
                        .header(OWNER_HEADER, "999"))
                .andExpect(status().isForbidden());
    }

    // Получение всех бронирований пользователя с состоянием ALL
    @Test
    void getAllByUser_ValidStateAll_ReturnsBookingList() throws Exception {
        Mockito.when(bookingService.getAllByUser(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .header(OWNER_HEADER, "2")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].booker.id", is(2)));
    }

    // Получение всех бронирований пользователя с состоянием CURRENT
    @Test
    void getAllByUser_ValidStateCurrent_ReturnsBookingList() throws Exception {
        Mockito.when(bookingService.getAllByUser(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .header(OWNER_HEADER, "2")
                        .param("state", "CURRENT")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)));
    }

    // Получение всех бронирований пользователя с состоянием PAST
    @Test
    void getAllByUser_ValidStatePast_ReturnsBookingList() throws Exception {
        Mockito.when(bookingService.getAllByUser(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .header(OWNER_HEADER, "2")
                        .param("state", "PAST")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)));
    }

    // Получение всех бронирований пользователя с состоянием FUTURE
    @Test
    void getAllByUser_ValidStateFuture_ReturnsBookingList() throws Exception {
        Mockito.when(bookingService.getAllByUser(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .header(OWNER_HEADER, "2")
                        .param("state", "FUTURE")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)));
    }

    // Получение всех бронирований пользователя с состоянием WAITING
    @Test
    void getAllByUser_ValidStateWaiting_ReturnsBookingList() throws Exception {
        Mockito.when(bookingService.getAllByUser(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .header(OWNER_HEADER, "2")
                        .param("state", "WAITING")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)));
    }

    // Получение всех бронирований пользователя с состоянием REJECTED
    @Test
    void getAllByUser_ValidStateRejected_ReturnsBookingList() throws Exception {
        Mockito.when(bookingService.getAllByUser(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .header(OWNER_HEADER, "2")
                        .param("state", "REJECTED")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)));
    }

    // Получение всех бронирований пользователя без параметров пагинации
    @Test
    void getAllByUser_WithoutPagination_ReturnsBookingList() throws Exception {
        Mockito.when(bookingService.getAllByUser(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .header(OWNER_HEADER, "2")
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)));
    }

    // Получение всех бронирований пользователя без заголовка
    @Test
    void getAllByUser_MissingUserIdHeader_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .param("state", "ALL"))
                .andExpect(status().isBadRequest());
    }

    // Получение всех бронирований пользователя с невалидным статусом
    @Test
    void getAllByUser_InvalidState_ReturnsBadRequest() throws Exception {
        Mockito.when(bookingService.getAllByUser(anyLong(), anyString(), anyInt(), anyInt()))
                .thenThrow(new ValidationException("Unknown state: INVALID_STATE"));

        mockMvc.perform(get("/bookings")
                        .header(OWNER_HEADER, "2")
                        .param("state", "INVALID_STATE"))
                .andExpect(status().isBadRequest());
    }

    // Получение всех бронирований пользователя с ValidationException
    @Test
    void getAllByUser_ValidationException_ReturnsBadRequest() throws Exception {
        Mockito.when(bookingService.getAllByUser(anyLong(), anyString(), anyInt(), anyInt()))
                .thenThrow(new ValidationException("Неверные параметры пагинации"));

        mockMvc.perform(get("/bookings")
                        .header(OWNER_HEADER, "2")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    // Получение всех бронирований владельца с состоянием ALL
    @Test
    void getAllByOwner_ValidStateAll_ReturnsBookingList() throws Exception {
        Mockito.when(bookingService.getAllByOwner(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings/owner")
                        .header(OWNER_HEADER, "1")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].item.id", is(1)));
    }

    // Получение всех бронирований владельца с состоянием CURRENT
    @Test
    void getAllByOwner_ValidStateCurrent_ReturnsBookingList() throws Exception {
        Mockito.when(bookingService.getAllByOwner(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings/owner")
                        .header(OWNER_HEADER, "1")
                        .param("state", "CURRENT")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)));
    }

    // Получение всех бронирований владельца с состоянием PAST
    @Test
    void getAllByOwner_ValidStatePast_ReturnsBookingList() throws Exception {
        Mockito.when(bookingService.getAllByOwner(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings/owner")
                        .header(OWNER_HEADER, "1")
                        .param("state", "PAST")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)));
    }

    // Получение всех бронирований владельца с состоянием FUTURE
    @Test
    void getAllByOwner_ValidStateFuture_ReturnsBookingList() throws Exception {
        Mockito.when(bookingService.getAllByOwner(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings/owner")
                        .header(OWNER_HEADER, "1")
                        .param("state", "FUTURE")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)));
    }

    // Получение всех бронирований владельца с состоянием WAITING
    @Test
    void getAllByOwner_ValidStateWaiting_ReturnsBookingList() throws Exception {
        Mockito.when(bookingService.getAllByOwner(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings/owner")
                        .header(OWNER_HEADER, "1")
                        .param("state", "WAITING")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)));
    }

    // Получение всех бронирований владельца с состоянием REJECTED
    @Test
    void getAllByOwner_ValidStateRejected_ReturnsBookingList() throws Exception {
        Mockito.when(bookingService.getAllByOwner(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings/owner")
                        .header(OWNER_HEADER, "1")
                        .param("state", "REJECTED")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)));
    }

    // Получение всех бронирований владельца без параметров пагинации
    @Test
    void getAllByOwner_WithoutPagination_ReturnsBookingList() throws Exception {
        Mockito.when(bookingService.getAllByOwner(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings/owner")
                        .header(OWNER_HEADER, "1")
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)));
    }

    // Получение всех бронирований владельца без заголовка
    @Test
    void getAllByOwner_MissingUserIdHeader_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .param("state", "ALL"))
                .andExpect(status().isBadRequest());
    }

    // Получение всех бронирований владельца с невалидным статусом
    @Test
    void getAllByOwner_InvalidState_ReturnsBadRequest() throws Exception {
        Mockito.when(bookingService.getAllByOwner(anyLong(), anyString(), anyInt(), anyInt()))
                .thenThrow(new ValidationException("Unknown state: INVALID_STATE"));

        mockMvc.perform(get("/bookings/owner")
                        .header(OWNER_HEADER, "1")
                        .param("state", "INVALID_STATE"))
                .andExpect(status().isBadRequest());
    }

    // Получение всех бронирований владельца с ValidationException
    @Test
    void getAllByOwner_ValidationException_ReturnsBadRequest() throws Exception {
        Mockito.when(bookingService.getAllByOwner(anyLong(), anyString(), anyInt(), anyInt()))
                .thenThrow(new ValidationException("Неверные параметры пагинации"));

        mockMvc.perform(get("/bookings/owner")
                        .header(OWNER_HEADER, "1")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    // Получение пустого списка бронирований владельца
    @Test
    void getAllByOwner_NoBookings_ReturnsEmptyList() throws Exception {
        Mockito.when(bookingService.getAllByOwner(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get("/bookings/owner")
                        .header(OWNER_HEADER, "1")
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(0)));
    }
}