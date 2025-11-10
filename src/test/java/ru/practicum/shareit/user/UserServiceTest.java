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
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.impl.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);
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

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.create(userCreateDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John", result.getName());
        assertEquals("john@example.com", result.getEmail());

        verify(userRepository, times(1)).existsByEmail("john@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    // Тест создания пользователя с существующим email - должен выбросить исключение
    @Test
    void createUser_DuplicateEmail_ThrowsException() {
        UserCreateDto userCreateDto = new UserCreateDto();
        userCreateDto.setName("John");
        userCreateDto.setEmail("john@example.com");

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(DatabaseUniqueConstraintException.class, () -> userService.create(userCreateDto));

        verify(userRepository, times(1)).existsByEmail("john@example.com");
        verify(userRepository, never()).save(any(User.class));
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

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.update(userDto, 1L);

        assertNotNull(result);
        assertEquals("UpdatedName", result.getName());
        assertEquals("john@example.com", result.getEmail());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    // Тест получения несуществующего пользователя - должен выбросить NotFoundException
    @Test
    void getUser_NonExistingId_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.retrieve(999L));

        verify(userRepository, times(1)).findById(999L);
    }

    // Тест удаления пользователя - должен вызвать метод удаления в Repository
    @Test
    void deleteUser_ExistingId_CallsRepository() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.delete(1L);

        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    // Тест получения списка всех пользователей
    @Test
    void getList_ReturnsUserList() {
        User user1 = new User();
        user1.setId(1L);
        user1.setName("John");
        user1.setEmail("john@example.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setName("Jane");
        user2.setEmail("jane@example.com");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserDto> result = userService.getList();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).getName());
        assertEquals("Jane", result.get(1).getName());

        verify(userRepository, times(1)).findAll();
    }

    // Тест обновления email на существующий - должен выбросить исключение
    @Test
    void updateUser_DuplicateEmail_ThrowsException() {
        UserDto userDto = new UserDto();
        userDto.setEmail("existing@example.com");

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("John");
        existingUser.setEmail("john@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(DatabaseUniqueConstraintException.class, () -> userService.update(userDto, 1L));

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }
}