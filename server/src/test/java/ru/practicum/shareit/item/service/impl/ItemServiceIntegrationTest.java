package ru.practicum.shareit.item.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dao.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScan(basePackages = {"ru.practicum.shareit"})
@Transactional
class ItemServiceIntegrationTest {

    @Autowired
    private ItemServiceImpl itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User owner;
    private User booker;
    private Item item;
    private Booking pastBooking;
    private Booking futureBooking;
    private Comment comment;

    @BeforeEach
    void setUp() {
        // Очистка базы данных перед каждым тестом
        commentRepository.deleteAll();
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        itemRequestRepository.deleteAll();
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

        // Создание прошедшего бронирования (last booking)
        pastBooking = new Booking();
        pastBooking.setStart(LocalDateTime.now().minusDays(2));
        pastBooking.setEnd(LocalDateTime.now().minusDays(1));
        pastBooking.setItem(item);
        pastBooking.setBooker(booker);
        pastBooking.setStatus(Status.APPROVED);
        pastBooking = bookingRepository.save(pastBooking);

        // Создание будущего бронирования (next booking)
        futureBooking = new Booking();
        futureBooking.setStart(LocalDateTime.now().plusDays(1));
        futureBooking.setEnd(LocalDateTime.now().plusDays(2));
        futureBooking.setItem(item);
        futureBooking.setBooker(booker);
        futureBooking.setStatus(Status.APPROVED);
        futureBooking = bookingRepository.save(futureBooking);

        // Создание комментария
        comment = new Comment();
        comment.setText("Test Comment");
        comment.setItem(item);
        comment.setAuthor(booker);
        comment.setCreated(LocalDateTime.now());
        comment = commentRepository.save(comment);
    }

    // Тест получения списка предметов с бронированиями и комментариями
    @Test
    void getList_WithValidUserId_ReturnsItemsWithBookingsAndComments() {
        // Выполнение тестируемого метода
        List<ItemDto> result = itemService.getList(owner.getId());

        // Проверка результатов
        assertNotNull(result);
        assertEquals(1, result.size());

        ItemDto itemDto = result.get(0);
        assertEquals(item.getId(), itemDto.getId());
        assertEquals(item.getName(), itemDto.getName());
        assertEquals(item.getDescription(), itemDto.getDescription());

        // Проверяем, что комментарии загружены
        assertNotNull(itemDto.getComments());
        assertEquals(1, itemDto.getComments().size());
        assertEquals(comment.getText(), itemDto.getComments().get(0).getText());
    }

    // Тест получения списка предметов для пользователя без предметов
    @Test
    void getList_UserWithoutItems_ReturnsEmptyList() {
        User newUser = new User();
        newUser.setName("New User");
        newUser.setEmail("newuser@example.com");
        newUser = userRepository.save(newUser);

        List<ItemDto> result = itemService.getList(newUser.getId());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Тест создания нового предмета
    @Test
    void create_WithValidData_ReturnsCreatedItem() {
        // Подготовка тестовых данных
        ItemCreateDto itemCreateDto = new ItemCreateDto();
        itemCreateDto.setName("New Item");
        itemCreateDto.setDescription("New Description");
        itemCreateDto.setAvailable(true);

        // Выполнение тестируемого метода
        ItemDto result = itemService.create(itemCreateDto, owner.getId());

        // Проверка результатов
        assertNotNull(result);
        assertEquals("New Item", result.getName());
        assertEquals("New Description", result.getDescription());
        assertTrue(result.getAvailable());

        // Проверяем, что предмет сохранен в базе
        List<Item> items = itemRepository.findAll();
        assertEquals(2, items.size()); // Старый + новый
    }

    // Тест создания предмета с несуществующим пользователем
    @Test
    void create_WithNonExistentUser_ThrowsNotFoundException() {
        ItemCreateDto itemCreateDto = new ItemCreateDto();
        itemCreateDto.setName("New Item");
        itemCreateDto.setDescription("New Description");
        itemCreateDto.setAvailable(true);

        assertThrows(NotFoundException.class, () ->
                itemService.create(itemCreateDto, 999L));
    }

    // Тест создания предмета с несуществующим requestId
    @Test
    void create_WithNonExistentRequestId_ThrowsNotFoundException() {
        ItemCreateDto itemCreateDto = new ItemCreateDto();
        itemCreateDto.setName("New Item");
        itemCreateDto.setDescription("New Description");
        itemCreateDto.setAvailable(true);
        itemCreateDto.setRequestId(999L);

        assertThrows(NotFoundException.class, () ->
                itemService.create(itemCreateDto, owner.getId()));
    }

    // Тест создания предмета с валидным requestId
    @Test
    void create_WithValidRequestId_ReturnsCreatedItem() {
        // Создаем запрос
        ItemRequest request = new ItemRequest();
        request.setDescription("Test Request");
        request.setAuthor(booker);
        request.setCreated(LocalDateTime.now());
        request = itemRequestRepository.save(request);

        ItemCreateDto itemCreateDto = new ItemCreateDto();
        itemCreateDto.setName("New Item");
        itemCreateDto.setDescription("New Description");
        itemCreateDto.setAvailable(true);
        itemCreateDto.setRequestId(request.getId());

        ItemDto result = itemService.create(itemCreateDto, owner.getId());

        assertNotNull(result);
        assertEquals("New Item", result.getName());
        assertEquals("New Description", result.getDescription());

        // Проверяем, что предмет связан с запросом
        Item createdItem = itemRepository.findById(result.getId()).orElseThrow();
        assertNotNull(createdItem.getRequest());
        assertEquals(request.getId(), createdItem.getRequest().getId());
    }

    // Тест получения конкретного предмета с деталями
    @Test
    void retrieve_WithValidIds_ReturnsItemWithDetails() {
        // Выполнение тестируемого метода
        ItemDto result = itemService.retrieve(item.getId(), owner.getId());

        // Проверка результатов
        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());

        // Должны быть комментарии
        assertNotNull(result.getComments());
        assertFalse(result.getComments().isEmpty());
    }

    // Тест получения предмета не владельцем
    @Test
    void retrieve_WithNonOwner_ReturnsItemWithoutBookings() {
        // Выполнение тестируемого метода для не владельца
        ItemDto result = itemService.retrieve(item.getId(), booker.getId());

        // Проверка результатов
        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());

        // Для не владельца не должно быть дат бронирований
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());

        // Но комментарии должны быть
        assertNotNull(result.getComments());
        assertFalse(result.getComments().isEmpty());
    }

    // Тест получения несуществующего предмета
    @Test
    void retrieve_WithNonExistentItem_ThrowsNotFoundException() {
        assertThrows(NotFoundException.class, () ->
                itemService.retrieve(999L, owner.getId()));
    }

    // Тест поиска предметов по тексту
    @Test
    void search_WithMatchingText_ReturnsItems() {
        // Выполнение тестируемого метода
        List<ItemDto> result = itemService.search("Test");

        // Проверка результатов
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(item.getName(), result.get(0).getName());
    }

    // Тест поиска по частичному совпадению в названии
    @Test
    void search_WithPartialNameMatch_ReturnsItems() {
        List<ItemDto> result = itemService.search("Item");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(item.getName(), result.get(0).getName());
    }

    // Тест поиска по частичному совпадению в описании
    @Test
    void search_WithPartialDescriptionMatch_ReturnsItems() {
        List<ItemDto> result = itemService.search("Description");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(item.getDescription(), result.get(0).getDescription());
    }

    // Тест поиска с неподходящим текстом
    @Test
    void search_WithNonMatchingText_ReturnsEmptyList() {
        // Выполнение тестируемого метода
        List<ItemDto> result = itemService.search("NonExistent");

        // Проверка результатов
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Тест поиска с пустым текстом
    @Test
    void search_WithEmptyText_ReturnsEmptyList() {
        // Выполнение тестируемого метода
        List<ItemDto> result = itemService.search("");

        // Проверка результатов
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Тест поиска с null текстом
    @Test
    void search_WithNullText_ReturnsEmptyList() {
        // Выполнение тестируемого метода
        List<ItemDto> result = itemService.search(null);

        // Проверка результатов
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Тест поиска недоступных предметов
    @Test
    void search_UnavailableItems_ReturnsEmptyList() {
        // Создаем недоступный предмет
        Item unavailableItem = new Item();
        unavailableItem.setName("Unavailable Item");
        unavailableItem.setDescription("Unavailable Description");
        unavailableItem.setIsAvailable(false);
        unavailableItem.setOwner(owner);
        itemRepository.save(unavailableItem);

        List<ItemDto> result = itemService.search("Unavailable");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Тест обновления предмета
    @Test
    void update_WithValidData_ReturnsUpdatedItem() {
        // Подготовка тестовых данных
        ItemDto updateDto = new ItemDto();
        updateDto.setName("Updated Name");
        updateDto.setDescription("Updated Description");
        updateDto.setAvailable(false);

        // Выполнение тестируемого метода
        ItemDto result = itemService.update(updateDto, item.getId(), owner.getId());

        // Проверка результатов
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertFalse(result.getAvailable());

        // Проверяем, что предмет обновлен в базе
        Item updatedItem = itemRepository.findById(item.getId()).orElseThrow();
        assertEquals("Updated Name", updatedItem.getName());
        assertEquals("Updated Description", updatedItem.getDescription());
        assertFalse(updatedItem.getIsAvailable());
    }

    // Тест обновления предмета с частичными данными - только название
    @Test
    void update_WithPartialDataOnlyName_ReturnsPartiallyUpdatedItem() {
        // Подготовка тестовых данных
        ItemDto updateDto = new ItemDto();
        updateDto.setName("Updated Name Only");

        // Выполнение тестируемого метода
        ItemDto result = itemService.update(updateDto, item.getId(), owner.getId());

        // Проверка результатов
        assertNotNull(result);
        assertEquals("Updated Name Only", result.getName());
        assertEquals(item.getDescription(), result.getDescription()); // Описание не изменилось
        assertTrue(result.getAvailable()); // Доступность не изменилась

        // Проверяем, что предмет обновлен в базе
        Item updatedItem = itemRepository.findById(item.getId()).orElseThrow();
        assertEquals("Updated Name Only", updatedItem.getName());
        assertEquals(item.getDescription(), updatedItem.getDescription());
        assertTrue(updatedItem.getIsAvailable());
    }

    // Тест обновления предмета с частичными данными - только описание
    @Test
    void update_WithPartialDataOnlyDescription_ReturnsPartiallyUpdatedItem() {
        // Подготовка тестовых данных
        ItemDto updateDto = new ItemDto();
        updateDto.setDescription("Updated Description Only");

        // Выполнение тестируемого метода
        ItemDto result = itemService.update(updateDto, item.getId(), owner.getId());

        // Проверка результатов
        assertNotNull(result);
        assertEquals(item.getName(), result.getName()); // Название не изменилось
        assertEquals("Updated Description Only", result.getDescription());
        assertTrue(result.getAvailable()); // Доступность не изменилась

        // Проверяем, что предмет обновлен в базе
        Item updatedItem = itemRepository.findById(item.getId()).orElseThrow();
        assertEquals(item.getName(), updatedItem.getName());
        assertEquals("Updated Description Only", updatedItem.getDescription());
        assertTrue(updatedItem.getIsAvailable());
    }

    // Тест обновления предмета с частичными данными - только доступность
    @Test
    void update_WithPartialDataOnlyAvailability_ReturnsPartiallyUpdatedItem() {
        // Подготовка тестовых данных
        ItemDto updateDto = new ItemDto();
        updateDto.setAvailable(false);

        // Выполнение тестируемого метода
        ItemDto result = itemService.update(updateDto, item.getId(), owner.getId());

        // Проверка результатов
        assertNotNull(result);
        assertEquals(item.getName(), result.getName()); // Название не изменилось
        assertEquals(item.getDescription(), result.getDescription()); // Описание не изменилось
        assertFalse(result.getAvailable());

        // Проверяем, что предмет обновлен в базе
        Item updatedItem = itemRepository.findById(item.getId()).orElseThrow();
        assertEquals(item.getName(), updatedItem.getName());
        assertEquals(item.getDescription(), updatedItem.getDescription());
        assertFalse(updatedItem.getIsAvailable());
    }

    // Тест обновления предмета с пустыми полями
    @Test
    void update_WithEmptyFields_ReturnsUnchangedItem() {
        // Подготовка тестовых данных
        ItemDto updateDto = new ItemDto();
        updateDto.setName("");
        updateDto.setDescription("");

        // Выполнение тестируемого метода
        ItemDto result = itemService.update(updateDto, item.getId(), owner.getId());

        // Проверка результатов
        assertNotNull(result);
        assertEquals(item.getName(), result.getName()); // Название не изменилось
        assertEquals(item.getDescription(), result.getDescription()); // Описание не изменилось
        assertTrue(result.getAvailable()); // Доступность не изменилась

        // Проверяем, что предмет не изменился в базе
        Item unchangedItem = itemRepository.findById(item.getId()).orElseThrow();
        assertEquals(item.getName(), unchangedItem.getName());
        assertEquals(item.getDescription(), unchangedItem.getDescription());
        assertTrue(unchangedItem.getIsAvailable());
    }

    // Тест обновления предмета не владельцем
    @Test
    void update_WithNonOwner_ThrowsAccessDeniedException() {
        ItemDto updateDto = new ItemDto();
        updateDto.setName("Updated Name");

        assertThrows(AccessDeniedException.class, () ->
                itemService.update(updateDto, item.getId(), booker.getId()));
    }

    // Тест обновления несуществующего предмета
    @Test
    void update_WithNonExistentItem_ThrowsNotFoundException() {
        ItemDto updateDto = new ItemDto();
        updateDto.setName("Updated Name");

        assertThrows(NotFoundException.class, () ->
                itemService.update(updateDto, 999L, owner.getId()));
    }

    // Тест обновления предмета с несуществующим пользователем
    @Test
    void update_WithNonExistentUser_ThrowsNotFoundException() {
        ItemDto updateDto = new ItemDto();
        updateDto.setName("Updated Name");

        assertThrows(NotFoundException.class, () ->
                itemService.update(updateDto, item.getId(), 999L));
    }

    // Тест получения предмета без бронирований
    @Test
    void retrieve_ItemWithoutBookings_ReturnsItemWithoutBookingDates() {
        // Создаем предмет без бронирований
        Item itemWithoutBookings = new Item();
        itemWithoutBookings.setName("Item Without Bookings");
        itemWithoutBookings.setDescription("Description Without Bookings");
        itemWithoutBookings.setIsAvailable(true);
        itemWithoutBookings.setOwner(owner);
        itemWithoutBookings = itemRepository.save(itemWithoutBookings);

        ItemDto result = itemService.retrieve(itemWithoutBookings.getId(), owner.getId());

        assertNotNull(result);
        assertEquals(itemWithoutBookings.getId(), result.getId());
        assertNull(result.getLastBooking()); // Не должно быть последнего бронирования
        assertNull(result.getNextBooking()); // Не должно быть следующего бронирования
    }

    // Тест получения предмета без комментариев
    @Test
    void retrieve_ItemWithoutComments_ReturnsItemWithoutComments() {
        // Создаем предмет без комментариев
        Item itemWithoutComments = new Item();
        itemWithoutComments.setName("Item Without Comments");
        itemWithoutComments.setDescription("Description Without Comments");
        itemWithoutComments.setIsAvailable(true);
        itemWithoutComments.setOwner(owner);
        itemWithoutComments = itemRepository.save(itemWithoutComments);

        ItemDto result = itemService.retrieve(itemWithoutComments.getId(), owner.getId());

        assertNotNull(result);
        assertEquals(itemWithoutComments.getId(), result.getId());
        assertNotNull(result.getComments());
        assertTrue(result.getComments().isEmpty());
    }

    // Тест получения списка с пользователем без предметов
    @Test
    void getList_WithNonExistentUser_ThrowsNotFoundException() {
        assertThrows(NotFoundException.class, () ->
                itemService.getList(999L));
    }

    // Тест создания предмета с существующим пользователем
    @Test
    void create_WithValidUser_ReturnsItem() {
        ItemCreateDto itemCreateDto = new ItemCreateDto();
        itemCreateDto.setName("Valid Item");
        itemCreateDto.setDescription("Valid Description");
        itemCreateDto.setAvailable(true);

        ItemDto result = itemService.create(itemCreateDto, owner.getId());

        assertNotNull(result);
        assertEquals("Valid Item", result.getName());
        assertEquals("Valid Description", result.getDescription());
        assertTrue(result.getAvailable());
    }
}