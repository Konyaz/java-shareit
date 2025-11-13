package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DatabaseUniqueConstraintException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class UserIntegrationTest {

    @Autowired
    private UserService userService;

    // Тест создания пользователя с валидными данными
    @Test
    void createUser_WithValidData_ShouldCreateUser() {
        UserCreateDto userCreateDto = new UserCreateDto();
        userCreateDto.setName("Integration User");
        userCreateDto.setEmail("integration@example.com");

        UserDto result = userService.create(userCreateDto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Integration User", result.getName());
        assertEquals("integration@example.com", result.getEmail());
    }

    // Тест создания пользователя с дублирующимся email
    @Test
    void createUser_WithDuplicateEmail_ShouldThrowException() {
        UserCreateDto firstUser = new UserCreateDto();
        firstUser.setName("First User");
        firstUser.setEmail("duplicate@example.com");

        UserCreateDto secondUser = new UserCreateDto();
        secondUser.setName("Second User");
        secondUser.setEmail("duplicate@example.com");

        userService.create(firstUser);

        assertThrows(DatabaseUniqueConstraintException.class, () -> userService.create(secondUser));
    }

    // Тест обновления пользователя с валидными данными
    @Test
    void updateUser_WithValidData_ShouldUpdateUser() {
        UserCreateDto createDto = new UserCreateDto();
        createDto.setName("Original Name");
        createDto.setEmail("original@example.com");

        UserDto createdUser = userService.create(createDto);

        UserDto updateDto = new UserDto();
        updateDto.setName("Updated Name");
        updateDto.setEmail("updated@example.com");

        UserDto result = userService.update(updateDto, createdUser.getId());

        assertNotNull(result);
        assertEquals(createdUser.getId(), result.getId());
        assertEquals("Updated Name", result.getName());
        assertEquals("updated@example.com", result.getEmail());
    }

    // Тест получения существующего пользователя
    @Test
    void getUser_WithExistingUser_ShouldReturnUser() {
        UserCreateDto createDto = new UserCreateDto();
        createDto.setName("Test User");
        createDto.setEmail("test@example.com");

        UserDto createdUser = userService.create(createDto);

        UserDto result = userService.retrieve(createdUser.getId());

        assertNotNull(result);
        assertEquals(createdUser.getId(), result.getId());
        assertEquals("Test User", result.getName());
        assertEquals("test@example.com", result.getEmail());
    }

    // Тест получения всех пользователей
    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        UserCreateDto user1 = new UserCreateDto();
        user1.setName("User One");
        user1.setEmail("user1@example.com");

        UserCreateDto user2 = new UserCreateDto();
        user2.setName("User Two");
        user2.setEmail("user2@example.com");

        userService.create(user1);
        userService.create(user2);

        List<UserDto> result = userService.getList();

        assertNotNull(result);
        assertTrue(result.size() >= 2);
    }

    // Тест удаления пользователя
    @Test
    void deleteUser_ShouldDeleteUser() {
        UserCreateDto createDto = new UserCreateDto();
        createDto.setName("User To Delete");
        createDto.setEmail("delete@example.com");

        UserDto createdUser = userService.create(createDto);

        assertDoesNotThrow(() -> userService.delete(createdUser.getId()));

        assertThrows(NotFoundException.class, () -> userService.retrieve(createdUser.getId()));
    }

    // Тест полного жизненного цикла пользователя
    @Test
    void userLifecycle_CreateUpdateDelete_ShouldWorkCorrectly() {
        UserCreateDto createDto = new UserCreateDto();
        createDto.setName("Lifecycle User");
        createDto.setEmail("lifecycle@example.com");

        UserDto createdUser = userService.create(createDto);
        assertNotNull(createdUser.getId());

        UserDto updateDto = new UserDto();
        updateDto.setName("Updated Lifecycle User");

        UserDto updatedUser = userService.update(updateDto, createdUser.getId());
        assertEquals("Updated Lifecycle User", updatedUser.getName());

        userService.delete(createdUser.getId());

        assertThrows(NotFoundException.class, () -> userService.retrieve(createdUser.getId()));
    }
}