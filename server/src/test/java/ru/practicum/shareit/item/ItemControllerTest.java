package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.CommentService;
import ru.practicum.shareit.item.service.ItemService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @MockBean
    private CommentService commentService;

    private ItemDto validItemDto;
    private ItemCreateDto validItemCreateDto;

    @BeforeEach
    void setUp() {
        validItemDto = new ItemDto();
        validItemDto.setId(1L);
        validItemDto.setName("Дрель");
        validItemDto.setDescription("Хорошая дрель");
        validItemDto.setAvailable(true);

        validItemCreateDto = new ItemCreateDto();
        validItemCreateDto.setName("Дрель");
        validItemCreateDto.setDescription("Хорошая дрель");
        validItemCreateDto.setAvailable(true);
    }

    // Тест успешного создания предмета
    @Test
    void createItem_ValidRequest_ReturnsCreated() throws Exception {
        when(itemService.create(any(ItemCreateDto.class), anyLong())).thenReturn(validItemDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validItemCreateDto))
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"))
                .andExpect(jsonPath("$.description").value("Хорошая дрель"))
                .andExpect(jsonPath("$.available").value(true));
    }

    // Тест успешного обновления предмета
    @Test
    void updateItem_ValidRequest_ReturnsOk() throws Exception {
        when(itemService.update(any(ItemDto.class), anyLong(), anyLong())).thenReturn(validItemDto);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validItemDto))
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"))
                .andExpect(jsonPath("$.description").value("Хорошая дрель"))
                .andExpect(jsonPath("$.available").value(true));
    }

    // Тест успешного получения предмета по ID
    @Test
    void getItemById_ValidRequest_ReturnsOk() throws Exception {
        when(itemService.retrieve(anyLong(), anyLong())).thenReturn(validItemDto);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"))
                .andExpect(jsonPath("$.description").value("Хорошая дрель"))
                .andExpect(jsonPath("$.available").value(true));
    }

    // Тест успешного получения всех предметов пользователя
    @Test
    void getAllItems_ValidRequest_ReturnsList() throws Exception {
        when(itemService.getList(anyLong())).thenReturn(List.of(validItemDto));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Дрель"))
                .andExpect(jsonPath("$[0].description").value("Хорошая дрель"))
                .andExpect(jsonPath("$[0].available").value(true));
    }

    // Тест успешного поиска предметов
    @Test
    void searchItems_ValidRequest_ReturnsList() throws Exception {
        when(itemService.search(anyString())).thenReturn(List.of(validItemDto));

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Дрель"))
                .andExpect(jsonPath("$[0].description").value("Хорошая дрель"))
                .andExpect(jsonPath("$[0].available").value(true));
    }

    // Тест создания предмета без заголовка пользователя
    @Test
    void createItem_MissingUserIdHeader_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validItemCreateDto))
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());
    }

    // Тест обновления предмета без заголовка пользователя
    @Test
    void updateItem_MissingUserIdHeader_ReturnsBadRequest() throws Exception {
        mockMvc.perform(patch("/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validItemDto))
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());
    }

    // Тест получения предмета по ID без заголовка пользователя
    @Test
    void getItemById_MissingUserIdHeader_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/items/1"))
                .andExpect(status().isBadRequest());
    }

    // Тест получения всех предметов без заголовка пользователя
    @Test
    void getAllItems_MissingUserIdHeader_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/items"))
                .andExpect(status().isBadRequest());
    }

    // Тест поиска предметов без параметра текста
    @Test
    void searchItems_MissingText_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/items/search"))
                .andExpect(status().isBadRequest());
    }

    // Тест получения пустого списка предметов
    @Test
    void getAllItems_EmptyList_ReturnsEmptyArray() throws Exception {
        when(itemService.getList(anyLong())).thenReturn(List.of());

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // Тест поиска с пустым текстом
    @Test
    void searchItems_EmptyText_ReturnsEmptyArray() throws Exception {
        when(itemService.search(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // Тест поиска без результатов
    @Test
    void searchItems_NoResults_ReturnsEmptyArray() throws Exception {
        when(itemService.search(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .param("text", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // Тест поиска с пробелами в тексте
    @Test
    void searchItems_WithSpacesInText_ReturnsList() throws Exception {
        when(itemService.search(anyString())).thenReturn(List.of(validItemDto));

        mockMvc.perform(get("/items/search")
                        .param("text", "  дрель  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }

    // Тест поиска со специальными символами
    @Test
    void searchItems_SpecialCharacters_ReturnsEmptyArray() throws Exception {
        when(itemService.search(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .param("text", "!@#$%"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // Тест получения всех предметов с пагинацией
    @Test
    void getAllItems_WithPagination_ReturnsList() throws Exception {
        when(itemService.getList(anyLong())).thenReturn(List.of(validItemDto));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }

    // Тест поиска предметов с пагинацией
    @Test
    void searchItems_WithPagination_ReturnsList() throws Exception {
        when(itemService.search(anyString())).thenReturn(List.of(validItemDto));

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }
}