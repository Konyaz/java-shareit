package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.practicum.shareit.exception.DatabaseUniqueConstraintException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.impl.UserServiceImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userDao);
    }

    // Тест создания пользователя с валидными данными - должен вернуть UserDto
    @Test
    void createUser_ValidData_ReturnsUserDto() {
        UserCreateDto userCreateDto = new UserCreateDto();
        userCreateDto.setName("John");
        userCreateDto.setEmail("john@example.com");

        User user = new User();
        user.setId(1L);
        user.setName("John");
        user.setEmail("john@example.com");

        when(userDao.isEmailExists("john@example.com")).thenReturn(false);
        when(userDao.create(any(User.class))).thenReturn(user);

        UserDto result = userService.create(userCreateDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John", result.getName());
        assertEquals("john@example.com", result.getEmail());
    }

    // Тест создания пользователя с существующим email - должен выбросить исключение
    @Test
    void createUser_DuplicateEmail_ThrowsException() {
        UserCreateDto userCreateDto = new UserCreateDto();
        userCreateDto.setName("John");
        userCreateDto.setEmail("john@example.com");

        when(userDao.isEmailExists("john@example.com")).thenReturn(true);

        assertThrows(DatabaseUniqueConstraintException.class, () -> userService.create(userCreateDto));
    }

    // Тест обновления пользователя с валидными данными - должен вернуть обновленного пользователя
    @Test
    void updateUser_ValidData_ReturnsUpdatedUser() {
        UserDto userDto = new UserDto();
        userDto.setName("UpdatedName");

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("John");
        existingUser.setEmail("john@example.com");

        when(userDao.exists(1L)).thenReturn(true);
        when(userDao.getById(1L)).thenReturn(existingUser);
        when(userDao.update(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.update(userDto, 1L);

        assertNotNull(result);
        assertEquals("UpdatedName", result.getName());
        assertEquals("john@example.com", result.getEmail());
    }

    // Тест получения несуществующего пользователя - должен выбросить NotFoundException
    @Test
    void getUser_NonExistingId_ThrowsException() {
        when(userDao.exists(999L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> userService.retrieve(999L));
    }

    // Тест удаления пользователя - должен вызвать метод удаления в DAO
    @Test
    void deleteUser_ExistingId_CallsDao() {
        when(userDao.exists(1L)).thenReturn(true);

        userService.delete(1L);

        verify(userDao, times(1)).removeById(1L);
    }
}