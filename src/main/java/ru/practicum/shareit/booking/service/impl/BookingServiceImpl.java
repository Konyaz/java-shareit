package ru.practicum.shareit.booking.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingStateParam;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements ru.practicum.shareit.booking.service.BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingDto create(BookingCreateDto bookingCreateDto, Long userId) {
        // ВАЖНО: сначала проверяем даты бронирования ДО обращения к репозиториям
        validateBookingDates(bookingCreateDto);

        User booker = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id={} не найден", userId);
                    return new NotFoundException(String.format("Пользователь с id=%s не найден", userId));
                });

        Item item = itemRepository.findById(bookingCreateDto.getItemId())
                .orElseThrow(() -> {
                    log.error("Вещь с id={} не найдена", bookingCreateDto.getItemId());
                    return new NotFoundException(String.format("Вещь с id=%s не найдена", bookingCreateDto.getItemId()));
                });

        if (!item.getIsAvailable()) {
            log.error("Вещь с id={} недоступна для бронирования", item.getId());
            throw new ValidationException("Вещь недоступна для бронирование");
        }

        if (item.getOwner().getId().equals(userId)) {
            log.error("Пользователь с id={} не может бронировать свою вещь", userId);
            throw new NotFoundException("Владелец не может бронировать свою вещь");
        }

        Booking booking = Booking.builder()
                .start(bookingCreateDto.getStart())
                .end(bookingCreateDto.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        log.info("Создано новое бронирование с ID: {} для вещи с ID: {}", savedBooking.getId(), item.getId());
        return BookingMapper.toBookingDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingDto approve(Long bookingId, Long userId, Boolean approved) {
        Booking booking = bookingRepository.findByIdWithItemAndBooker(bookingId)
                .orElseThrow(() -> {
                    log.error("Бронирование с id={} не найдено", bookingId);
                    return new NotFoundException(String.format("Бронирование с id=%s не найдено", bookingId));
                });

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            log.error("Пользователь с id={} не является владельцем вещи", userId);
            throw new AccessDeniedException("Только владелец вещи может подтверждать бронирование");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            log.error("Бронирование с id={} уже обработано", bookingId);
            throw new ValidationException("Бронирование уже обработано");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);

        String statusMessage = approved ? "подтверждено" : "отклонено";
        log.info("Бронирование с ID: {} {}", bookingId, statusMessage);
        return BookingMapper.toBookingDto(updatedBooking);
    }

    @Override
    @Transactional
    public BookingDto cancel(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findByIdWithItemAndBooker(bookingId)
                .orElseThrow(() -> {
                    log.error("Бронирование с id={} не найдено", bookingId);
                    return new NotFoundException(String.format("Бронирование с id=%s не найдено", bookingId));
                });

        if (!booking.getBooker().getId().equals(userId)) {
            log.error("Пользователь с id={} не является создателем бронирования", userId);
            throw new AccessDeniedException("Только создатель бронирования может отменить его");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            log.error("Бронирование с id={} уже обработано, нельзя отменить", bookingId);
            throw new ValidationException("Невозможно отменить уже обработанное бронирование");
        }

        booking.setStatus(BookingStatus.CANCELED);
        Booking updatedBooking = bookingRepository.save(booking);

        log.info("Бронирование с ID: {} отменено пользователем с ID: {}", bookingId, userId);
        return BookingMapper.toBookingDto(updatedBooking);
    }

    @Override
    public BookingDto getById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findByIdWithItemAndBooker(bookingId)
                .orElseThrow(() -> {
                    log.error("Бронирование с id={} не найдено", bookingId);
                    return new NotFoundException(String.format("Бронирование с id=%s не найдено", bookingId));
                });

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            log.error("Пользователь с id={} не имеет доступа к бронированию с id={}", userId, bookingId);
            throw new AccessDeniedException("Доступ к бронированию запрещен");
        }

        log.debug("Получено бронирование с ID: {} для пользователя с ID: {}", bookingId, userId);
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getUserBookings(Long userId, String state) {
        if (!userRepository.existsById(userId)) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException(String.format("Пользователь с id=%s не найден", userId));
        }

        List<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();
        Sort sort = Sort.by(Sort.Direction.DESC, "start");

        try {
            BookingStateParam stateParam = BookingStateParam.valueOf(state.toUpperCase());

            switch (stateParam) {
                case ALL:
                    bookings = bookingRepository.findByBookerId(userId, sort);
                    break;
                case CURRENT:
                    bookings = bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(
                            userId, now, now, sort);
                    break;
                case PAST:
                    bookings = bookingRepository.findByBookerIdAndEndBefore(userId, now, sort);
                    break;
                case FUTURE:
                    bookings = bookingRepository.findByBookerIdAndStartAfter(userId, now, sort);
                    break;
                case WAITING:
                    bookings = bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING, sort);
                    break;
                case REJECTED:
                    bookings = bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED, sort);
                    break;
                case CANCELED:
                    bookings = bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.CANCELED, sort);
                    break;
                default:
                    log.error("Неизвестный статус бронирования: {}", state);
                    throw new ValidationException("Unknown state: " + state);
            }
        } catch (IllegalArgumentException e) {
            log.error("Неизвестный статус бронирования: {}", state);
            throw new ValidationException("Unknown state: " + state);
        }

        log.debug("Получено {} бронирований пользователя с ID: {} со статусом: {}",
                bookings.size(), userId, state);
        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long userId, String state) {
        if (!userRepository.existsById(userId)) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException(String.format("Пользователь с id=%s не найден", userId));
        }

        List<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();
        Sort sort = Sort.by(Sort.Direction.DESC, "start");

        try {
            BookingStateParam stateParam = BookingStateParam.valueOf(state.toUpperCase());

            switch (stateParam) {
                case ALL:
                    bookings = bookingRepository.findByItemOwnerId(userId, sort);
                    break;
                case CURRENT:
                    bookings = bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfter(
                            userId, now, now, sort);
                    break;
                case PAST:
                    bookings = bookingRepository.findByItemOwnerIdAndEndBefore(userId, now, sort);
                    break;
                case FUTURE:
                    bookings = bookingRepository.findByItemOwnerIdAndStartAfter(userId, now, sort);
                    break;
                case WAITING:
                    bookings = bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.WAITING, sort);
                    break;
                case REJECTED:
                    bookings = bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.REJECTED, sort);
                    break;
                case CANCELED:
                    bookings = bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.CANCELED, sort);
                    break;
                default:
                    log.error("Неизвестный статус бронирования: {}", state);
                    throw new ValidationException("Unknown state: " + state);
            }
        } catch (IllegalArgumentException e) {
            log.error("Неизвестный статус бронирования: {}", state);
            throw new ValidationException("Unknown state: " + state);
        }

        log.debug("Получено {} бронирований для вещей владельца с ID: {} со статусом: {}",
                bookings.size(), userId, state);
        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    private void validateBookingDates(BookingCreateDto bookingCreateDto) {
        // Проверка корректности дат бронирования (ДО обращения к репозиториям)
        if (bookingCreateDto.getStart().isAfter(bookingCreateDto.getEnd()) ||
                bookingCreateDto.getStart().isEqual(bookingCreateDto.getEnd())) {
            log.error("Некорректные даты бронирования: start={}, end={}",
                    bookingCreateDto.getStart(), bookingCreateDto.getEnd());
            throw new ValidationException("Дата начала должна быть раньше даты окончания");
        }

        // Проверка что дата начала не в прошлом (согласно требованиям Postman-тестов)
        if (bookingCreateDto.getStart().isBefore(LocalDateTime.now())) {
            log.error("Дата начала бронирования не может быть в прошлом: start={}", bookingCreateDto.getStart());
            throw new ValidationException("Дата начала бронирования не может быть в прошлом");
        }

        // Проверка что дата окончания не в прошлом (согласно требованиям Postman-тестов)
        if (bookingCreateDto.getEnd().isBefore(LocalDateTime.now())) {
            log.error("Дата окончания бронирования не может быть в прошлом: end={}", bookingCreateDto.getEnd());
            throw new ValidationException("Дата окончания бронирования не может быть в прошлом");
        }
    }
}