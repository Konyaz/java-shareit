package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ShareItTests {

    // Тест загрузки контекста Spring - проверяет, что приложение запускается корректно
    @Test
    void contextLoads() {
    }

}