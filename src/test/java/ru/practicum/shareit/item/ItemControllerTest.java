package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingInfoDto;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@ActiveProfiles("test")
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
    private ItemUpdateDto updateItemDto;
    private ItemDto searchItemDto;
    private CommentCreateDto commentCreateDto;
    private CommentDto commentDto;
    private ItemWithBookingsDto itemWithBookingsDto;
    private ItemDetailedDto itemDetailedDto;

    @BeforeEach
    void init() {
        // Инициализация ItemCreateDto объектов
        validItemCreateDto = new ItemCreateDto();
        validItemCreateDto.setName("Item");
        validItemCreateDto.setDescription("Description");
        validItemCreateDto.setAvailable(true);

        invalidItemCreateDto = new ItemCreateDto();
        invalidItemCreateDto.setName("");

        // Инициализация BookingInfoDto объектов
        BookingInfoDto lastBooking = new BookingInfoDto();
        lastBooking.setId(1L);
        lastBooking.setBookerId(2L);

        BookingInfoDto nextBooking = new BookingInfoDto();
        nextBooking.setId(2L);
        nextBooking.setBookerId(3L);

        // Инициализация CommentDto
        commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Great item!");
        commentDto.setAuthorName("John");
        commentDto.setCreated(LocalDateTime.now());

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

        updateItemDto = new ItemUpdateDto();
        updateItemDto.setName("UpdatedItem");
        updateItemDto.setDescription("Description");
        updateItemDto.setAvailable(true);

        searchItemDto = new ItemDto();
        searchItemDto.setId(1L);
        searchItemDto.setName("Item");

        // Инициализация CommentCreateDto
        commentCreateDto = new CommentCreateDto();
        commentCreateDto.setText("Great item!");

        // Инициализация ItemWithBookingsDto
        itemWithBookingsDto = new ItemWithBookingsDto();
        itemWithBookingsDto.setId(1L);
        itemWithBookingsDto.setName("Item");
        itemWithBookingsDto.setDescription("Description");
        itemWithBookingsDto.setAvailable(true);
        itemWithBookingsDto.setLastBooking(lastBooking);
        itemWithBookingsDto.setNextBooking(nextBooking);
        itemWithBookingsDto.setComments(List.of(commentDto));

        // Инициализация ItemDetailedDto
        itemDetailedDto = new ItemDetailedDto();
        itemDetailedDto.setId(1L);
        itemDetailedDto.setName("Item");
        itemDetailedDto.setDescription("Description");
        itemDetailedDto.setAvailable(true);
        itemDetailedDto.setLastBooking(lastBooking);
        itemDetailedDto.setNextBooking(nextBooking);
        itemDetailedDto.setComments(List.of(commentDto));
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
        Mockito.when(itemService.update(any(ItemUpdateDto.class), anyLong(), anyLong())).thenReturn(updatedItemDto);

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
        Mockito.when(itemService.update(any(ItemUpdateDto.class), anyLong(), anyLong()))
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
        Mockito.when(itemService.getList(1L)).thenReturn(List.of(itemWithBookingsDto));

        mockMvc.perform(get("/items")
                        .header(ItemController.OWNER_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Item")))
                .andExpect(jsonPath("$[0].lastBooking.id", is(1)))
                .andExpect(jsonPath("$[0].nextBooking.id", is(2)))
                .andExpect(jsonPath("$[0].comments[0].text", is("Great item!")));
    }

    // Тест получения конкретного предмета по ID - должен вернуть предмет
    @Test
    void getItemValidIdsReturnsItem() throws Exception {
        Mockito.when(itemService.retrieve(1L, 1L)).thenReturn(itemDetailedDto);

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

    // Тест добавления комментария с валидными данными - должен вернуть комментарий
    @Test
    void addComment_ValidData_ReturnsComment() throws Exception {
        Mockito.when(itemService.addComment(anyLong(), anyLong(), any(CommentCreateDto.class)))
                .thenReturn(commentDto);

        mockMvc.perform(post("/items/1/comment")
                        .header(ItemController.OWNER_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.text", is("Great item!")))
                .andExpect(jsonPath("$.authorName", is("John")));
    }

    // Тест добавления комментария без заголовка пользователя - должен вернуть ошибку
    @Test
    void addComment_MissingUserId_ReturnsError() throws Exception {
        mockMvc.perform(post("/items/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isInternalServerError());
    }

    // Тест добавления комментария к несуществующему предмету - должен вернуть 404
    @Test
    void addComment_ItemNotFound_ReturnsNotFound() throws Exception {
        Mockito.when(itemService.addComment(anyLong(), anyLong(), any(CommentCreateDto.class)))
                .thenThrow(new NotFoundException("Предмет с id=999 не найден"));

        mockMvc.perform(post("/items/999/comment")
                        .header(ItemController.OWNER_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isNotFound());
    }

    // Тест добавления комментария пользователем, который не брал вещь - должен вернуть 400
    @Test
    void addComment_UserNotBooker_ReturnsBadRequest() throws Exception {
        Mockito.when(itemService.addComment(anyLong(), anyLong(), any(CommentCreateDto.class)))
                .thenThrow(new ValidationException("Нельзя оставить комментарий к вещи, которую вы не брали в аренду"));

        mockMvc.perform(post("/items/1/comment")
                        .header(ItemController.OWNER_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isBadRequest());
    }

    // Тест добавления комментария с пустым текстом - должен вернуть 400
    @Test
    void addComment_EmptyText_ReturnsBadRequest() throws Exception {
        CommentCreateDto emptyComment = new CommentCreateDto();
        emptyComment.setText("");

        mockMvc.perform(post("/items/1/comment")
                        .header(ItemController.OWNER_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyComment)))
                .andExpect(status().isBadRequest());
    }
}