package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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

    private ItemCreateDto validItemCreateDto;
    private ItemCreateDto invalidItemCreateDto;
    private ItemDto itemDto;
    private ItemDto updatedItemDto;
    private ItemDto updateItemDto;
    private ItemDto searchItemDto;

    @BeforeEach
    void init() {
        // Инициализация ItemCreateDto объектов
        validItemCreateDto = new ItemCreateDto();
        validItemCreateDto.setName("Item");
        validItemCreateDto.setDescription("Description");
        validItemCreateDto.setAvailable(true);

        invalidItemCreateDto = new ItemCreateDto();
        invalidItemCreateDto.setName("");

        // Инициализация ItemDto объектов
        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Item");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);

        updatedItemDto = new ItemDto();
        updatedItemDto.setId(1L);
        updatedItemDto.setName("UpdatedItem");
        updatedItemDto.setDescription("Description");
        updatedItemDto.setAvailable(true);

        updateItemDto = new ItemDto();
        updateItemDto.setName("UpdatedItem");

        searchItemDto = new ItemDto();
        searchItemDto.setId(1L);
        searchItemDto.setName("Item");
    }

    // Тест создания предмета с валидными данными - должен вернуть созданный предмет
    @Test
    void createItemValidDataReturnsItemDto() throws Exception {
        Mockito.when(itemService.create(any(ItemCreateDto.class), anyLong())).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header(ItemController.OWNER_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validItemCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Item")))
                .andExpect(jsonPath("$.description", is("Description")))
                .andExpect(jsonPath("$.available", is(true)));
    }

    // Тест создания предмета без заголовка пользователя - должен вернуть ошибку сервера
    @Test
    void createItemMissingUserIdHeaderReturnsInternalError() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validItemCreateDto)))
                .andExpect(status().isInternalServerError());
    }

    // Тест создания предмета для несуществующего пользователя - должен вернуть 404
    @Test
    void createItemUserNotFoundReturnsNotFound() throws Exception {
        Mockito.when(itemService.create(any(ItemCreateDto.class), anyLong()))
                .thenThrow(new NotFoundException("Пользователь с id=999 не найден"));

        mockMvc.perform(post("/items")
                        .header(ItemController.OWNER_HEADER, "999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validItemCreateDto)))
                .andExpect(status().isNotFound());
    }

    // Тест обновления предмета с валидными данными - должен вернуть обновленный предмет
    @Test
    void updateItemValidDataReturnsUpdatedItem() throws Exception {
        Mockito.when(itemService.update(any(ItemDto.class), anyLong(), anyLong())).thenReturn(updatedItemDto);

        mockMvc.perform(patch("/items/1")
                        .header(ItemController.OWNER_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateItemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("UpdatedItem")));
    }

    // Тест обновления предмета без прав доступа - должен вернуть 403 Forbidden
    @Test
    void updateItemAccessDeniedReturnsForbidden() throws Exception {
        Mockito.when(itemService.update(any(ItemDto.class), anyLong(), anyLong()))
                .thenThrow(new AccessDeniedException("Доступ запрещен"));

        mockMvc.perform(patch("/items/1")
                        .header(ItemController.OWNER_HEADER, "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateItemDto)))
                .andExpect(status().isForbidden());
    }

    // Тест получения списка предметов пользователя - должен вернуть список предметов
    @Test
    void getItemsValidUserIdReturnsItemList() throws Exception {
        Mockito.when(itemService.getList(1L)).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items")
                        .header(ItemController.OWNER_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Item")));
    }

    // Тест получения конкретного предмета по ID - должен вернуть предмет
    @Test
    void getItemValidIdsReturnsItem() throws Exception {
        Mockito.when(itemService.retrieve(1L, 1L)).thenReturn(itemDto);

        mockMvc.perform(get("/items/1")
                        .header(ItemController.OWNER_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    // Тест получения несуществующего предмета - должен вернуть 404 Not Found
    @Test
    void getItemItemNotFoundReturnsNotFound() throws Exception {
        Mockito.when(itemService.retrieve(999L, 1L))
                .thenThrow(new NotFoundException("Предмет с id=999 не найден"));

        mockMvc.perform(get("/items/999")
                        .header(ItemController.OWNER_HEADER, "1"))
                .andExpect(status().isNotFound());
    }

    // Тест поиска предметов по тексту - должен вернуть найденные предметы
    @Test
    void searchItemsValidTextReturnsItems() throws Exception {
        Mockito.when(itemService.search("test")).thenReturn(List.of(searchItemDto));

        mockMvc.perform(get("/items/search")
                        .param("text", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    // Тест поиска с пустым текстом - должен вернуть пустой список
    @Test
    void searchItemsEmptyTextReturnsEmptyList() throws Exception {
        Mockito.when(itemService.search("")).thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(0)));
    }

    // Тест поиска без параметра text - должен вернуть ошибку сервера
    @Test
    void searchItems_MissingText_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/items/search"))
                .andExpect(status().isInternalServerError());
    }
}