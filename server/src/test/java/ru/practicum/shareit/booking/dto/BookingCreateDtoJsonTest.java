package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingCreateDtoJsonTest {

    @Autowired
    private JacksonTester<BookingCreateDto> json;

    @Autowired
    private ObjectMapper objectMapper;

    // Тест сериализации объекта BookingCreateDto в JSON
    @Test
    void testSerialize() throws Exception {
        // Подготовка тестовых данных
        BookingCreateDto bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(1L);
        LocalDateTime start = LocalDateTime.of(2023, 12, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2023, 12, 2, 10, 0);
        bookingCreateDto.setStart(start);
        bookingCreateDto.setEnd(end);

        // Выполнение сериализации
        JsonContent<BookingCreateDto> result = json.write(bookingCreateDto);

        // Проверка результатов сериализации
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start")
                .isEqualTo(start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertThat(result).extractingJsonPathStringValue("$.end")
                .isEqualTo(end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    // Тест десериализации JSON в объект BookingCreateDto
    @Test
    void testDeserialize() throws Exception {
        // Подготовка JSON строки
        String content = "{\"itemId\":1,\"start\":\"2023-12-01T10:00:00\",\"end\":\"2023-12-02T10:00:00\"}";

        // Выполнение десериализации
        BookingCreateDto result = json.parseObject(content);

        // Проверка результатов десериализации
        assertThat(result.getItemId()).isEqualTo(1L);
        assertThat(result.getStart()).isEqualTo(LocalDateTime.of(2023, 12, 1, 10, 0));
        assertThat(result.getEnd()).isEqualTo(LocalDateTime.of(2023, 12, 2, 10, 0));
    }

    // Тест десериализации с отсутствующими полями
    @Test
    void testDeserializeWithMissingFields() throws Exception {
        // Подготовка JSON строки с отсутствующими полями
        String content = "{\"itemId\":1}";

        // Выполнение десериализации
        BookingCreateDto result = json.parseObject(content);

        // Проверка что отсутствующие поля обрабатываются корректно
        assertThat(result.getItemId()).isEqualTo(1L);
        assertThat(result.getStart()).isNull();
        assertThat(result.getEnd()).isNull();
    }

    // Тест методов equals и hashCode
    @Test
    void testEqualsAndHashCode() {
        // Подготовка тестовых данных
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingCreateDto dto1 = new BookingCreateDto();
        dto1.setItemId(1L);
        dto1.setStart(start);
        dto1.setEnd(end);

        BookingCreateDto dto2 = new BookingCreateDto();
        dto2.setItemId(1L);
        dto2.setStart(start);
        dto2.setEnd(end);

        BookingCreateDto dto3 = new BookingCreateDto();
        dto3.setItemId(2L);
        dto3.setStart(start);
        dto3.setEnd(end);

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
        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(1L);
        LocalDateTime start = LocalDateTime.of(2023, 12, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2023, 12, 2, 10, 0);
        dto.setStart(start);
        dto.setEnd(end);

        // Вызов метода toString
        String toString = dto.toString();

        // Проверка содержимого строки
        assertThat(toString).contains("itemId=1");
        assertThat(toString).contains("start=" + start);
        assertThat(toString).contains("end=" + end);
    }
}