package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    public static final String USER_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public BookingDto create(@RequestHeader(USER_HEADER) Long userId,
                             @RequestBody BookingCreateDto bookingCreateDto) {
        log.info("POST /bookings -> {} | userid={}", bookingCreateDto, userId);
        return bookingService.create(bookingCreateDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@RequestHeader(USER_HEADER) Long userId,
                              @PathVariable Long bookingId,
                              @RequestParam Boolean approved) {
        log.info("PATCH /bookings/{}?approved={} | userid={}", bookingId, approved, userId);
        return bookingService.approve(bookingId, userId, approved);
    }

    @PatchMapping("/{bookingId}/cancel")
    public BookingDto cancel(@RequestHeader(USER_HEADER) Long userId,
                             @PathVariable Long bookingId) {
        log.info("PATCH /bookings/{}/cancel | userid={}", bookingId, userId);
        return bookingService.cancel(bookingId, userId);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getById(@RequestHeader(USER_HEADER) Long userId,
                              @PathVariable Long bookingId) {
        log.info("GET /bookings/{} | userid={}", bookingId, userId);
        return bookingService.getById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getUserBookings(@RequestHeader(USER_HEADER) Long userId,
                                            @RequestParam(defaultValue = "ALL") String state) {
        log.info("GET /bookings?state={} | userid={}", state, userId);
        return bookingService.getUserBookings(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookings(@RequestHeader(USER_HEADER) Long userId,
                                             @RequestParam(defaultValue = "ALL") String state) {
        log.info("GET /bookings/owner?state={} | userid={}", state, userId);
        return bookingService.getOwnerBookings(userId, state);
    }
}