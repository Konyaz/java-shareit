package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemRequestDtoTest {

    // Тест конструктора без параметров
    @Test
    void testNoArgsConstructor() {
        // Выполнение теста
        ItemRequestDto dto = new ItemRequestDto();

        // Проверка результатов
        assertThat(dto.getId()).isNull();
        assertThat(dto.getDescription()).isNull();
        assertThat(dto.getCreated()).isNull();
        assertThat(dto.getItems()).isNull();
    }

    // Тест установки всех полей через сеттеры
    @Test
    void testAllArgsConstructor() {
        // Подготовка тестовых данных
        Long id = 1L;
        String description = "Test description";
        LocalDateTime created = LocalDateTime.now();
        List<ItemDto> items = new ArrayList<>();

        // Выполнение теста
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(id);
        dto.setDescription(description);
        dto.setCreated(created);
        dto.setItems(items);

        // Проверка результатов
        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getDescription()).isEqualTo(description);
        assertThat(dto.getCreated()).isEqualTo(created);
        assertThat(dto.getItems()).isEqualTo(items);
    }

    // Тест методов equals и hashCode
    @Test
    void testEqualsAndHashCode() {
        // Подготовка тестовых данных
        LocalDateTime created = LocalDateTime.now();

        ItemRequestDto dto1 = new ItemRequestDto();
        dto1.setId(1L);
        dto1.setDescription("Description 1");
        dto1.setCreated(created);

        ItemRequestDto dto2 = new ItemRequestDto();
        dto2.setId(1L);
        dto2.setDescription("Description 1");
        dto2.setCreated(created);

        ItemRequestDto dto3 = new ItemRequestDto();
        dto3.setId(2L);
        dto3.setDescription("Description 2");
        dto3.setCreated(created);

        // Проверка равенства объектов и хэш-кодов
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1).isNotEqualTo(dto3);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.hashCode()).isNotEqualTo(dto3.hashCode());
    }

    // Тест метода toString
    @Test
    void testToString() {
        // Подготовка тестовых данных
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(1L);
        dto.setDescription("Test description");
        dto.setCreated(LocalDateTime.of(2023, 12, 1, 10, 0));

        // Выполнение теста
        String toString = dto.toString();

        // Проверка результатов
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("description=Test description");
        assertThat(toString).contains("created=2023-12-01T10:00");
    }

    // Тест работы со списком предметов
    @Test
    void testWithItems() {
        // Подготовка тестовых данных
        ItemDto item1 = new ItemDto();
        item1.setId(1L);
        item1.setName("Item 1");

        ItemDto item2 = new ItemDto();
        item2.setId(2L);
        item2.setName("Item 2");

        List<ItemDto> items = List.of(item1, item2);

        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(1L);
        dto.setItems(items);

        // Проверка результатов
        assertThat(dto.getItems()).hasSize(2);
        assertThat(dto.getItems().get(0).getName()).isEqualTo("Item 1");
        assertThat(dto.getItems().get(1).getName()).isEqualTo("Item 2");
    }

    // Тест с пустым списком предметов
    @Test
    void testEmptyItemsList() {
        // Подготовка тестовых данных
        ItemRequestDto dto = new ItemRequestDto();
        dto.setItems(new ArrayList<>());

        // Проверка результатов
        assertThat(dto.getItems()).isNotNull();
        assertThat(dto.getItems()).isEmpty();
    }

    // Тест с null списком предметов
    @Test
    void testNullItems() {
        // Подготовка тестовых данных
        ItemRequestDto dto = new ItemRequestDto();
        dto.setItems(null);

        // Проверка результатов
        assertThat(dto.getItems()).isNull();
    }

    // Тест установки временной метки создания
    @Test
    void testCreatedTimestamp() {
        // Подготовка тестовых данных
        LocalDateTime now = LocalDateTime.now();
        ItemRequestDto dto = new ItemRequestDto();
        dto.setCreated(now);

        // Проверка результатов
        assertThat(dto.getCreated()).isEqualTo(now);
    }

    // Тест с null значениями всех полей
    @Test
    void testNullFields() {
        // Подготовка тестовых данных
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(null);
        dto.setDescription(null);
        dto.setCreated(null);
        dto.setItems(null);

        // Проверка результатов
        assertThat(dto.getId()).isNull();
        assertThat(dto.getDescription()).isNull();
        assertThat(dto.getCreated()).isNull();
        assertThat(dto.getItems()).isNull();
    }
}