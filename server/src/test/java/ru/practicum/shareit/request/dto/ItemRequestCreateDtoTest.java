package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

class ItemRequestCreateDtoTest {

    @Autowired
    private JacksonTester<ItemRequestCreateDto> json;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Тест конструктора без параметров
    @Test
    void testNoArgsConstructor() {
        // Выполнение теста
        ItemRequestCreateDto dto = new ItemRequestCreateDto();

        // Проверка результатов
        assertThat(dto.getDescription()).isNull();
    }

    // Тест установки описания через сеттер
    @Test
    void testAllArgsConstructor() {
        // Выполнение теста
        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription("Test description");

        // Проверка результатов
        assertThat(dto.getDescription()).isEqualTo("Test description");
    }

    // Тест методов equals и hashCode
    @Test
    void testEqualsAndHashCode() {
        // Подготовка тестовых данных
        ItemRequestCreateDto dto1 = new ItemRequestCreateDto();
        dto1.setDescription("Description 1");

        ItemRequestCreateDto dto2 = new ItemRequestCreateDto();
        dto2.setDescription("Description 1");

        ItemRequestCreateDto dto3 = new ItemRequestCreateDto();
        dto3.setDescription("Description 2");

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
        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription("Test description");

        // Выполнение теста
        String toString = dto.toString();

        // Проверка результатов
        assertThat(toString).contains("description=Test description");
    }

    // Тест JSON сериализации
    @Test
    void testJsonSerialization() throws Exception {
        // Подготовка тестовых данных
        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription("Test description");

        // Выполнение сериализации
        String jsonString = objectMapper.writeValueAsString(dto);

        // Проверка результатов
        assertThat(jsonString).contains("Test description");
    }

    // Тест JSON десериализации
    @Test
    void testJsonDeserialization() throws Exception {
        // Подготовка JSON строки
        String json = "{\"description\":\"Test description\"}";

        // Выполнение десериализации
        ItemRequestCreateDto dto = objectMapper.readValue(json, ItemRequestCreateDto.class);

        // Проверка результатов
        assertThat(dto.getDescription()).isEqualTo("Test description");
    }

    // Тест с null описанием
    @Test
    void testNullDescription() {
        // Подготовка тестовых данных
        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription(null);

        // Проверка результатов
        assertThat(dto.getDescription()).isNull();
    }

    // Тест с пустым описанием
    @Test
    void testEmptyDescription() {
        // Подготовка тестовых данных
        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription("");

        // Проверка результатов
        assertThat(dto.getDescription()).isEmpty();
    }

    // Тест с описанием из пробелов
    @Test
    void testBlankDescription() {
        // Подготовка тестовых данных
        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription("   ");

        // Проверка результатов
        assertThat(dto.getDescription()).isEqualTo("   ");
    }
}