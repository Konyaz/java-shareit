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
    void createBookingValidDataReturnsBookingDto() throws Exception {
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
    void createBookingMissingUserIdHeaderReturnsInternalServerError() throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBookingCreateDto)))
                .andExpect(status().isInternalServerError());
    }

    // Создание бронирования для несуществующего предмета
    @Test
    void createBookingItemNotFoundReturnsNotFound() throws Exception {
        Mockito.when(bookingService.create(anyLong(), any(BookingCreateDto.class)))
                .thenThrow(new NotFoundException("Вещь с id=999 не найдена"));

        mockMvc.perform(post("/bookings")
                        .header(OWNER_HEADER, "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBookingCreateDto)))
                .andExpect(status().isNotFound());
    }

    // Подтверждение бронирования с валидными данными
    @Test
    void approveBookingValidDataReturnsApprovedBooking() throws Exception {
        Mockito.when(bookingService.approve(anyLong(), anyLong(), anyBoolean())).thenReturn(approvedBookingDto);

        mockMvc.perform(patch("/bookings/1")
                        .header(OWNER_HEADER, "1")
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }

    // Подтверждение бронирования без заголовка пользователя
    @Test
    void approveBookingMissingUserIdHeaderReturnsInternalServerError() throws Exception {
        mockMvc.perform(patch("/bookings/1")
                        .param("approved", "true"))
                .andExpect(status().isInternalServerError());
    }

    // Подтверждение бронирования без прав доступа
    @Test
    void approveBookingAccessDeniedReturnsForbidden() throws Exception {
        Mockito.when(bookingService.approve(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new AccessDeniedException("Пользователь не является владельцем вещи"));

        mockMvc.perform(patch("/bookings/1")
                        .header(OWNER_HEADER, "2")
                        .param("approved", "true"))
                .andExpect(status().isForbidden());
    }

    // Подтверждение несуществующего бронирования
    @Test
    void approveBookingNotFoundReturnsNotFound() throws Exception {
        Mockito.when(bookingService.approve(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new NotFoundException("Бронирование с id=999 не найдено"));

        mockMvc.perform(patch("/bookings/999")
                        .header(OWNER_HEADER, "1")
                        .param("approved", "true"))
                .andExpect(status().isNotFound());
    }

    // Получение бронирования по ID
    @Test
    void getBookingValidDataReturnsBooking() throws Exception {
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
    void getBookingMissingUserIdHeaderReturnsInternalServerError() throws Exception {
        mockMvc.perform(get("/bookings/1"))
                .andExpect(status().isInternalServerError());
    }

    // Получение несуществующего бронирования
    @Test
    void getBookingNotFoundReturnsNotFound() throws Exception {
        Mockito.when(bookingService.get(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Бронирование с id=999 не найдено"));

        mockMvc.perform(get("/bookings/999")
                        .header(OWNER_HEADER, "1"))
                .andExpect(status().isNotFound());
    }

    // Получение всех бронирований пользователя
    @Test
    void getAllByUserValidDataReturnsBookingList() throws Exception {
        Mockito.when(bookingService.getAllByUser(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .header(OWNER_HEADER, "2")
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].booker.id", is(2)));
    }

    // Получение всех бронирований пользователя без заголовка
    @Test
    void getAllByUserMissingUserIdHeaderReturnsInternalServerError() throws Exception {
        mockMvc.perform(get("/bookings")
                        .param("state", "ALL"))
                .andExpect(status().isInternalServerError());
    }

    // Получение всех бронирований пользователя с невалидным статусом
    @Test
    void getAllByUserInvalidStateReturnsBadRequest() throws Exception {
        Mockito.when(bookingService.getAllByUser(anyLong(), anyString(), anyInt(), anyInt()))
                .thenThrow(new ValidationException("Unknown state: INVALID_STATE"));

        mockMvc.perform(get("/bookings")
                        .header(OWNER_HEADER, "2")
                        .param("state", "INVALID_STATE"))
                .andExpect(status().isBadRequest());
    }

    // Получение всех бронирований владельца
    @Test
    void getAllByOwnerValidDataReturnsBookingList() throws Exception {
        Mockito.when(bookingService.getAllByOwner(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings/owner")
                        .header(OWNER_HEADER, "1")
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].item.id", is(1)));
    }

    // Получение всех бронирований владельца без заголовка
    @Test
    void getAllByOwnerMissingUserIdHeaderReturnsInternalServerError() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .param("state", "ALL"))
                .andExpect(status().isInternalServerError());
    }

    // Получение всех бронирований владельца с невалидным статусом
    @Test
    void getAllByOwnerInvalidStateReturnsBadRequest() throws Exception {
        Mockito.when(bookingService.getAllByOwner(anyLong(), anyString(), anyInt(), anyInt()))
                .thenThrow(new ValidationException("Unknown state: INVALID_STATE"));

        mockMvc.perform(get("/bookings/owner")
                        .header(OWNER_HEADER, "1")
                        .param("state", "INVALID_STATE"))
                .andExpect(status().isBadRequest());
    }
}