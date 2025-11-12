package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import static ru.practicum.shareit.Constant.OWNER_HEADER;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient client;

    // Создание нового бронирования
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(@RequestHeader(OWNER_HEADER) @Positive @NotNull long userId,
                                         @RequestBody @Valid BookingCreateRequestDto requestDto) {
        log.info("Creating booking {}, userId={}", requestDto, userId);
        return client.create(userId, requestDto);
    }

    // Подтверждение или отклонение бронирования
    @PatchMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> approve(@RequestHeader(OWNER_HEADER) @Positive @NotNull long userId,
                                          @PathVariable @Positive @NotNull long bookingId,
                                          @RequestParam boolean approved) {
        log.info("Получен запрос на подтверждение бронирования с id={} от пользователя с id={}", bookingId, userId);
        return client.approve(userId, bookingId, approved);
    }

    // Получение информации о бронировании по ID
    @GetMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> get(@RequestHeader(OWNER_HEADER) @Positive @NotNull long userId,
                                      @PathVariable @Positive @NotNull long bookingId) {
        log.info("Get booking {}, userId={}", bookingId, userId);
        return client.get(userId, bookingId);
    }

    // Получение всех бронирований пользователя
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> getAllByUser(@RequestHeader(OWNER_HEADER) long userId,
                                               @RequestParam(name = "state", defaultValue = "all") String stateParam,
                                               @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                               @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
        return client.getAllByUser(userId, state, from, size);
    }

    // Получение всех бронирований для предметов владельца
    @GetMapping("/owner")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> getAllByOwner(@RequestHeader(OWNER_HEADER) Long userId,
                                                @RequestParam(defaultValue = "ALL") String stateParam,
                                                @RequestParam(defaultValue = "0") Integer from,
                                                @RequestParam(defaultValue = "10") Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.info("Получен запрос на получение всех бронирований владельца с id={}, state={}, from={}, size={}", userId,
                state, from, size);
        return client.getAllByOwner(userId, state, from, size);
    }
}