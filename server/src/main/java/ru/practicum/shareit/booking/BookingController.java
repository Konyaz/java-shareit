package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

import static ru.practicum.shareit.Constant.OWNER_HEADER;

@Slf4j
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // Создание нового бронирования
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDto create(@RequestHeader(OWNER_HEADER) Long userId,
                             @RequestBody BookingCreateDto bookingCreateDto) {
        log.info("Получен запрос на создание бронирования от пользователя с id={}", userId);
        return bookingService.create(userId, bookingCreateDto);
    }

    // Подтверждение или отклонение бронирования
    @PatchMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.OK)
    public BookingDto approve(@RequestHeader(OWNER_HEADER) Long userId,
                              @PathVariable Long bookingId,
                              @RequestParam Boolean approved) {
        log.info("Получен запрос на подтверждение бронирования с id={} от пользователя с id={}", bookingId, userId);
        return bookingService.approve(userId, bookingId, approved);
    }

    // Получение информации о бронировании по ID
    @GetMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.OK)
    public BookingDto get(@RequestHeader(OWNER_HEADER) Long userId,
                          @PathVariable Long bookingId) {
        log.info("Получен запрос на получение бронирования с id={} от пользователя с id={}", bookingId, userId);
        return bookingService.get(userId, bookingId);
    }

    // Получение всех бронирований пользователя
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<BookingDto> getAllByUser(@RequestHeader(OWNER_HEADER) Long userId,
                                         @RequestParam(defaultValue = "ALL") String state,
                                         @RequestParam(defaultValue = "0") Integer from,
                                         @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получен запрос на получение всех бронирований пользователя с id={}, state={}, from={}, size={}",
                userId, state, from, size);
        return bookingService.getAllByUser(userId, state, from, size);
    }

    // Получение всех бронирований для предметов владельца
    @GetMapping("/owner")
    @ResponseStatus(HttpStatus.OK)
    public List<BookingDto> getAllByOwner(@RequestHeader(OWNER_HEADER) Long userId,
                                          @RequestParam(defaultValue = "ALL") String state,
                                          @RequestParam(defaultValue = "0") Integer from,
                                          @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получен запрос на получение всех бронирований владельца с id={}, state={}, from={}, size={}", userId,
                state, from, size);
        return bookingService.getAllByOwner(userId, state, from, size);
    }
}