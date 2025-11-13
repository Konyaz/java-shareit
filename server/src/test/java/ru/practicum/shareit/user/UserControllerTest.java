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
import ru.practicum.shareit.exception.ValidationException;
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
    void createUser_ValidData_ReturnsUserDto() throws Exception {
        Mockito.when(userService.create(any(UserCreateDto.class))).thenReturn(userDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John")))
                .andExpect(jsonPath("$.email", is("john@example.com")));
    }

    // Создание пользователя с ValidationException
    @Test
    void createUser_ValidationException_ReturnsBadRequest() throws Exception {
        Mockito.when(userService.create(any(UserCreateDto.class)))
                .thenThrow(new ValidationException("Неверные данные пользователя"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserCreateDto)))
                .andExpect(status().isBadRequest());
    }

    // Обновление пользователя с валидными данными
    @Test
    void updateUser_ValidData_ReturnsUpdatedUser() throws Exception {
        Mockito.when(userService.update(any(UserDto.class), anyLong())).thenReturn(updatedUserDto);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("UpdatedName")));
    }

    // Обновление пользователя с ValidationException
    @Test
    void updateUser_ValidationException_ReturnsBadRequest() throws Exception {
        Mockito.when(userService.update(any(UserDto.class), anyLong()))
                .thenThrow(new ValidationException("Неверные данные для обновления"));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest());
    }

    // Обновление несуществующего пользователя
    @Test
    void updateUser_NotFoundException_ReturnsNotFound() throws Exception {
        Mockito.when(userService.update(any(UserDto.class), anyLong()))
                .thenThrow(new NotFoundException("Пользователь с id=999 не найден"));

        mockMvc.perform(patch("/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    // Получение пользователя по существующему ID
    @Test
    void getUser_ExistingId_ReturnsUser() throws Exception {
        Mockito.when(userService.retrieve(1L)).thenReturn(userDto);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John")));
    }

    // Получение пользователя по несуществующему ID
    @Test
    void getUser_NonExistingId_ReturnsNotFound() throws Exception {
        Mockito.when(userService.retrieve(999L))
                .thenThrow(new NotFoundException("Пользователь с id=999 не найден"));

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());
    }

    // Получение всех пользователей
    @Test
    void getAllUsers_ReturnsUserList() throws Exception {
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
    void deleteUser_ExistingId_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(userService).delete(1L);
    }

    // Удаление несуществующего пользователя
    @Test
    void deleteUser_NotFoundException_ReturnsNotFound() throws Exception {
        Mockito.doThrow(new NotFoundException("Пользователь с id=999 не найден"))
                .when(userService).delete(999L);

        mockMvc.perform(delete("/users/999"))
                .andExpect(status().isNotFound());
    }

    // Создание пользователя с дублирующимся email
    @Test
    void createUser_DuplicateEmail_ReturnsConflict() throws Exception {
        Mockito.when(userService.create(any(UserCreateDto.class)))
                .thenThrow(new DatabaseUniqueConstraintException("Указанная почта уже зарегистрирована в приложении"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserCreateDto)))
                .andExpect(status().isConflict());
    }
}