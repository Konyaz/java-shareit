package ru.practicum.shareit.booking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dao.Status;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;
    private User owner;
    private User booker;
    private Item item;
    private Booking booking;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;

        // Очистка базы данных перед каждым тестом
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        // Создание пользователей
        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@example.com");
        owner = userRepository.save(owner);

        booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@example.com");
        booker = userRepository.save(booker);

        // Создание предмета
        item = new Item();
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setIsAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);

        // Создание бронирования
        booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Status.WAITING);
        booking = bookingRepository.save(booking);
    }

    private HttpHeaders createHeaders(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", userId.toString());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // Интеграционный тест создания бронирования
    @Test
    void createBooking_Integration_ReturnsCreatedBooking() {
        // Подготовка данных
        BookingCreateDto bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(item.getId());
        bookingCreateDto.setStart(LocalDateTime.now().plusDays(3));
        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(4));

        HttpEntity<BookingCreateDto> request = new HttpEntity<>(bookingCreateDto, createHeaders(booker.getId()));

        // Выполнение запроса
        ResponseEntity<BookingDto> response = restTemplate.exchange(
                baseUrl + "/bookings",
                HttpMethod.POST,
                request,
                BookingDto.class
        );

        // Проверка результатов
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertNotNull(response.getBody());
        assertEquals(bookingCreateDto.getItemId(), response.getBody().getItem().getId());
        assertEquals(Status.WAITING, response.getBody().getStatus());

        // Проверка что бронирование сохранено в БД
        List<Booking> bookings = bookingRepository.findAll();
        assertEquals(2, bookings.size()); // Исходное + новое
    }

    // Интеграционный тест получения бронирования по ID
    @Test
    void getBooking_Integration_ReturnsBooking() {
        HttpEntity<Void> request = new HttpEntity<>(createHeaders(booker.getId()));

        ResponseEntity<BookingDto> response = restTemplate.exchange(
                baseUrl + "/bookings/" + booking.getId(),
                HttpMethod.GET,
                request,
                BookingDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(response.getBody());
        assertEquals(booking.getId(), response.getBody().getId());
        assertEquals(item.getId(), response.getBody().getItem().getId());
        assertEquals(booker.getId(), response.getBody().getBooker().getId());
    }

    // Интеграционный тест получения всех бронирований пользователя
    @Test
    void getAllByUser_Integration_ReturnsBookingList() {
        HttpEntity<Void> request = new HttpEntity<>(createHeaders(booker.getId()));

        ResponseEntity<BookingDto[]> response = restTemplate.exchange(
                baseUrl + "/bookings?state=ALL&from=0&size=10",
                HttpMethod.GET,
                request,
                BookingDto[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().length);
        assertEquals(booking.getId(), response.getBody()[0].getId());
    }

    // Интеграционный тест получения всех бронирований владельца
    @Test
    void getAllByOwner_Integration_ReturnsBookingList() {
        HttpEntity<Void> request = new HttpEntity<>(createHeaders(owner.getId()));

        ResponseEntity<BookingDto[]> response = restTemplate.exchange(
                baseUrl + "/bookings/owner?state=ALL&from=0&size=10",
                HttpMethod.GET,
                request,
                BookingDto[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().length);
        assertEquals(booking.getId(), response.getBody()[0].getId());
    }

    // Интеграционный тест создания бронирования с невалидными данными
    @Test
    void createBooking_InvalidData_ReturnsBadRequest() {
        BookingCreateDto invalidBookingCreateDto = new BookingCreateDto();
        invalidBookingCreateDto.setItemId(item.getId());
        invalidBookingCreateDto.setStart(LocalDateTime.now().plusDays(3));
        invalidBookingCreateDto.setEnd(LocalDateTime.now().plusDays(2)); // Неправильные даты

        HttpEntity<BookingCreateDto> request = new HttpEntity<>(invalidBookingCreateDto, createHeaders(booker.getId()));

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/bookings",
                HttpMethod.POST,
                request,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // Интеграционный тест создания бронирования для недоступного предмета
    @Test
    void createBooking_UnavailableItem_ReturnsBadRequest() {
        // Создаем недоступный предмет
        Item unavailableItem = new Item();
        unavailableItem.setName("Unavailable Item");
        unavailableItem.setDescription("Unavailable Description");
        unavailableItem.setIsAvailable(false);
        unavailableItem.setOwner(owner);
        unavailableItem = itemRepository.save(unavailableItem);

        BookingCreateDto bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(unavailableItem.getId());
        bookingCreateDto.setStart(LocalDateTime.now().plusDays(3));
        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(4));

        HttpEntity<BookingCreateDto> request = new HttpEntity<>(bookingCreateDto, createHeaders(booker.getId()));

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/bookings",
                HttpMethod.POST,
                request,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // Интеграционный тест получения несуществующего бронирования
    @Test
    void getBooking_NotFound_ReturnsNotFound() {
        HttpEntity<Void> request = new HttpEntity<>(createHeaders(booker.getId()));

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/bookings/999",
                HttpMethod.GET,
                request,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // Интеграционный тест получения бронирований с разными состояниями
    @Test
    void getAllByUser_DifferentStates_ReturnsCorrectBookings() {
        // Создаем завершенное бронирование
        Booking pastBooking = new Booking();
        pastBooking.setStart(LocalDateTime.now().minusDays(3));
        pastBooking.setEnd(LocalDateTime.now().minusDays(2));
        pastBooking.setItem(item);
        pastBooking.setBooker(booker);
        pastBooking.setStatus(Status.APPROVED);
        bookingRepository.save(pastBooking);

        HttpEntity<Void> request = new HttpEntity<>(createHeaders(booker.getId()));

        // Тестируем состояние PAST
        ResponseEntity<BookingDto[]> pastResponse = restTemplate.exchange(
                baseUrl + "/bookings?state=PAST&from=0&size=10",
                HttpMethod.GET,
                request,
                BookingDto[].class
        );

        assertThat(pastResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(pastResponse.getBody());
        assertEquals(1, pastResponse.getBody().length); // Только завершенное бронирование

        // Тестируем состояние FUTURE
        ResponseEntity<BookingDto[]> futureResponse = restTemplate.exchange(
                baseUrl + "/bookings?state=FUTURE&from=0&size=10",
                HttpMethod.GET,
                request,
                BookingDto[].class
        );

        assertThat(futureResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(futureResponse.getBody());
        assertEquals(1, futureResponse.getBody().length); // Только будущее бронирование
    }

    // Интеграционный тест пагинации
    @Test
    void getAllByUser_WithPagination_ReturnsPaginatedResults() {
        // Создаем несколько бронирований
        for (int i = 0; i < 5; i++) {
            Booking newBooking = new Booking();
            newBooking.setStart(LocalDateTime.now().plusDays(i + 5));
            newBooking.setEnd(LocalDateTime.now().plusDays(i + 6));
            newBooking.setItem(item);
            newBooking.setBooker(booker);
            newBooking.setStatus(Status.WAITING);
            bookingRepository.save(newBooking);
        }

        HttpEntity<Void> request = new HttpEntity<>(createHeaders(booker.getId()));

        // Запрашиваем первую страницу с 3 элементами
        ResponseEntity<BookingDto[]> response = restTemplate.exchange(
                baseUrl + "/bookings?state=ALL&from=0&size=3",
                HttpMethod.GET,
                request,
                BookingDto[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().length); // Должно вернуть 3 элемента
    }

    // Интеграционный тест создания бронирования собственного предмета
    @Test
    void createBooking_OwnItem_ReturnsNotFound() {
        BookingCreateDto bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(item.getId());
        bookingCreateDto.setStart(LocalDateTime.now().plusDays(3));
        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(4));

        // Владелец пытается забронировать свою вещь
        HttpEntity<BookingCreateDto> request = new HttpEntity<>(bookingCreateDto, createHeaders(owner.getId()));

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/bookings",
                HttpMethod.POST,
                request,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}