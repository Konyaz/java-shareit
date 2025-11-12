package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.DatabaseUniqueConstraintException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserCreateDto validUserCreateDto;
    private UserCreateDto invalidUserCreateDto;
    private UserDto userDto;
    private UserDto updatedUserDto;
    private UserDto updateDto;

    @BeforeEach
    void init() {
        validUserCreateDto = new UserCreateDto();
        validUserCreateDto.setName("John");
        validUserCreateDto.setEmail("john@example.com");

        invalidUserCreateDto = new UserCreateDto();
        invalidUserCreateDto.setName("");

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("John");
        userDto.setEmail("john@example.com");

        updatedUserDto = new UserDto();
        updatedUserDto.setId(1L);
        updatedUserDto.setName("UpdatedName");
        updatedUserDto.setEmail("john@example.com");

        updateDto = new UserDto();
        updateDto.setName("UpdatedName");
    }

    // Создание пользователя с валидными данными
    @Test
    void createUserValidDataReturnsUserDto() throws Exception {
        Mockito.when(userService.create(any(UserCreateDto.class))).thenReturn(userDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John")))
                .andExpect(jsonPath("$.email", is("john@example.com")));
    }

    // Обновление пользователя с валидными данными
    @Test
    void updateUserValidDataReturnsUpdatedUser() throws Exception {
        Mockito.when(userService.update(any(UserDto.class), anyLong())).thenReturn(updatedUserDto);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("UpdatedName")));
    }

    // Получение пользователя по существующему ID
    @Test
    void getUserExistingIdReturnsUser() throws Exception {
        Mockito.when(userService.retrieve(1L)).thenReturn(userDto);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John")));
    }

    // Получение пользователя по несуществующему ID
    @Test
    void getUserNonExistingIdReturnsNotFound() throws Exception {
        Mockito.when(userService.retrieve(999L))
                .thenThrow(new NotFoundException("Пользователь с id=999 не найден"));

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());
    }

    // Получение всех пользователей
    @Test
    void getAllUsersReturnsUserList() throws Exception {
        UserDto user2 = new UserDto();
        user2.setId(2L);
        user2.setName("Jane");
        user2.setEmail("jane@example.com");

        Mockito.when(userService.getList()).thenReturn(List.of(userDto, user2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("John")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Jane")));
    }

    // Удаление пользователя по существующему ID
    @Test
    void deleteUserExistingIdReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(userService).delete(1L);
    }

    // Создание пользователя с дублирующимся email
    @Test
    void createUserDuplicateEmailReturnsConflict() throws Exception {
        Mockito.when(userService.create(any(UserCreateDto.class)))
                .thenThrow(new DatabaseUniqueConstraintException("Указанная почта уже зарегистрирована в приложении"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserCreateDto)))
                .andExpect(status().isConflict());
    }
}