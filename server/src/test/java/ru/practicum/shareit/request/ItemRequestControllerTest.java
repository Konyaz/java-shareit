package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.Constant.OWNER_HEADER;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private ItemRequestCreateDto validItemRequestCreateDto;
    private ItemRequestDto itemRequestDto;
    private ItemRequestDto itemRequestWithItemsDto;
    private ItemDto itemDto;

    @BeforeEach
    void init() {
        validItemRequestCreateDto = new ItemRequestCreateDto();
        validItemRequestCreateDto.setDescription("Хотел бы взять в аренду дрель");

        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Дрель");
        itemDto.setDescription("Простая дрель");
        itemDto.setAvailable(true);

        itemRequestDto = new ItemRequestDto();
        itemRequestDto.setId(1L);
        itemRequestDto.setDescription("Хотел бы взять в аренду дрель");
        itemRequestDto.setCreated(LocalDateTime.now());
        itemRequestDto.setItems(List.of());

        itemRequestWithItemsDto = new ItemRequestDto();
        itemRequestWithItemsDto.setId(1L);
        itemRequestWithItemsDto.setDescription("Хотел бы взять в аренду дрель");
        itemRequestWithItemsDto.setCreated(LocalDateTime.now());
        itemRequestWithItemsDto.setItems(List.of(itemDto));
    }

    // Создание запроса с валидными данными
    @Test
    void createRequestValidDataReturnsItemRequestDto() throws Exception {
        Mockito.when(itemRequestService.create(any(ItemRequestCreateDto.class), anyLong()))
                .thenReturn(itemRequestDto);

        mockMvc.perform(post("/requests")
                        .header(OWNER_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validItemRequestCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("Хотел бы взять в аренду дрель")))
                .andExpect(jsonPath("$.created").exists())
                .andExpect(jsonPath("$.items").isArray());
    }

    // Создание запроса без заголовка пользователя
    @Test
    void createRequestMissingUserIdHeaderReturnsInternalServerError() throws Exception {
        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validItemRequestCreateDto)))
                .andExpect(status().isInternalServerError());
    }

    // Создание запроса для несуществующего пользователя
    @Test
    void createRequestUserNotFoundReturnsNotFound() throws Exception {
        Mockito.when(itemRequestService.create(any(ItemRequestCreateDto.class), anyLong()))
                .thenThrow(new NotFoundException("Пользователь с id=999 не найден"));

        mockMvc.perform(post("/requests")
                        .header(OWNER_HEADER, "999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validItemRequestCreateDto)))
                .andExpect(status().isNotFound());
    }

    // Получение списка своих запросов
    @Test
    void getMyListValidUserIdReturnsItemRequestList() throws Exception {
        Mockito.when(itemRequestService.getMyList(1L)).thenReturn(List.of(itemRequestWithItemsDto));

        mockMvc.perform(get("/requests")
                        .header(OWNER_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].description", is("Хотел бы взять в аренду дрель")))
                .andExpect(jsonPath("$[0].items.length()", is(1)))
                .andExpect(jsonPath("$[0].items[0].id", is(1)));
    }

    // Получение списка запросов для несуществующего пользователя
    @Test
    void getMyListUserNotFoundReturnsNotFound() throws Exception {
        Mockito.when(itemRequestService.getMyList(999L))
                .thenThrow(new NotFoundException("Пользователь с id=999 не найден"));

        mockMvc.perform(get("/requests")
                        .header(OWNER_HEADER, "999"))
                .andExpect(status().isNotFound());
    }

    // Получение списка запросов без заголовка пользователя
    @Test
    void getMyListMissingUserIdHeaderReturnsInternalServerError() throws Exception {
        mockMvc.perform(get("/requests"))
                .andExpect(status().isInternalServerError());
    }

    // Получение конкретного запроса по ID
    @Test
    void retrieveValidRequestIdReturnsItemRequest() throws Exception {
        Mockito.when(itemRequestService.retrieve(1L)).thenReturn(itemRequestWithItemsDto);

        mockMvc.perform(get("/requests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("Хотел бы взять в аренду дрель")))
                .andExpect(jsonPath("$.items.length()", is(1)))
                .andExpect(jsonPath("$.items[0].id", is(1)));
    }

    // Получение несуществующего запроса
    @Test
    void retrieveRequestNotFoundReturnsNotFound() throws Exception {
        Mockito.when(itemRequestService.retrieve(999L))
                .thenThrow(new NotFoundException("Запрос с id=999 не найден"));

        mockMvc.perform(get("/requests/999"))
                .andExpect(status().isNotFound());
    }

    // Получение общего списка запросов
    @Test
    void getCommonListReturnsItemRequestList() throws Exception {
        Mockito.when(itemRequestService.getCommonList()).thenReturn(List.of(itemRequestWithItemsDto));

        mockMvc.perform(get("/requests/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].description", is("Хотел бы взять в аренду дрель")))
                .andExpect(jsonPath("$[0].items.length()", is(1)))
                .andExpect(jsonPath("$[0].items[0].id", is(1)));
    }
}