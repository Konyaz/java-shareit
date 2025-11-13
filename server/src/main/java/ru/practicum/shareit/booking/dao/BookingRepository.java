package ru.practicum.shareit.booking.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Поиск бронирований по ID пользователя
    List<Booking> findByBookerId(Long bookerId, Sort sort);

    // Поиск бронирований по ID пользователя с пагинацией - НОВЫЙ МЕТОД
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId")
    List<Booking> findByBookerIdWithPagination(@Param("bookerId") Long bookerId, Pageable pageable);

    // Поиск завершенных бронирований пользователя
    List<Booking> findByBookerIdAndEndIsBefore(Long bookerId, LocalDateTime end, Sort sort);

    // Поиск завершенных бронирований пользователя с пагинацией - НОВЫЙ МЕТОД
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.end < :end")
    List<Booking> findByBookerIdAndEndIsBeforeWithPagination(@Param("bookerId") Long bookerId,
                                                             @Param("end") LocalDateTime end,
                                                             Pageable pageable);

    // Поиск будущих бронирований пользователя
    List<Booking> findByBookerIdAndStartIsAfter(Long bookerId, LocalDateTime start, Sort sort);

    // Поиск будущих бронирований пользователя с пагинацией - НОВЫЙ МЕТОД
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.start > :start")
    List<Booking> findByBookerIdAndStartIsAfterWithPagination(@Param("bookerId") Long bookerId,
                                                              @Param("start") LocalDateTime start,
                                                              Pageable pageable);

    // Поиск текущих бронирований пользователя
    List<Booking> findByBookerIdAndStartIsBeforeAndEndIsAfter(Long bookerId, LocalDateTime start, LocalDateTime end,
                                                              Sort sort);

    // Поиск текущих бронирований пользователя с пагинацией - НОВЫЙ МЕТОД
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.start < :start AND b.end > :end")
    List<Booking> findByBookerIdAndStartIsBeforeAndEndIsAfterWithPagination(@Param("bookerId") Long bookerId,
                                                                            @Param("start") LocalDateTime start,
                                                                            @Param("end") LocalDateTime end,
                                                                            Pageable pageable);

    // Поиск бронирований пользователя по статусу
    List<Booking> findByBookerIdAndStatusIs(Long bookerId, Status status, Sort sort);

    // Поиск бронирований пользователя по статусу с пагинацией - НОВЫЙ МЕТОД
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.status = :status")
    List<Booking> findByBookerIdAndStatusIsWithPagination(@Param("bookerId") Long bookerId,
                                                          @Param("status") Status status,
                                                          Pageable pageable);

    // Поиск бронирований по списку ID предметов
    List<Booking> findByItemIdIn(List<Long> itemIds, Sort sort);

    // Поиск бронирований по списку ID предметов с пагинацией - НОВЫЙ МЕТОД
    @Query("SELECT b FROM Booking b WHERE b.item.id IN :itemIds")
    List<Booking> findByItemIdInWithPagination(@Param("itemIds") List<Long> itemIds, Pageable pageable);

    // Поиск завершенных бронирований по списку предметов
    List<Booking> findByItemIdInAndEndIsBefore(List<Long> itemIds, LocalDateTime end, Sort sort);

    // Поиск завершенных бронирований по списку предметов с пагинацией - НОВЫЙ МЕТОД
    @Query("SELECT b FROM Booking b WHERE b.item.id IN :itemIds AND b.end < :end")
    List<Booking> findByItemIdInAndEndIsBeforeWithPagination(@Param("itemIds") List<Long> itemIds,
                                                             @Param("end") LocalDateTime end,
                                                             Pageable pageable);

    // Поиск будущих бронирований по списку предметов
    List<Booking> findByItemIdInAndStartIsAfter(List<Long> itemIds, LocalDateTime start, Sort sort);

    // Поиск будущих бронирований по списку предметов с пагинацией - НОВЫЙ МЕТОД
    @Query("SELECT b FROM Booking b WHERE b.item.id IN :itemIds AND b.start > :start")
    List<Booking> findByItemIdInAndStartIsAfterWithPagination(@Param("itemIds") List<Long> itemIds,
                                                              @Param("start") LocalDateTime start,
                                                              Pageable pageable);

    // Поиск текущих бронирований по списку предметов
    List<Booking> findByItemIdInAndStartIsBeforeAndEndIsAfter(List<Long> itemIds, LocalDateTime start,
                                                              LocalDateTime end, Sort sort);

    // Поиск текущих бронирований по списку предметов с пагинацией - НОВЫЙ МЕТОД
    @Query("SELECT b FROM Booking b WHERE b.item.id IN :itemIds AND b.start < :start AND b.end > :end")
    List<Booking> findByItemIdInAndStartIsBeforeAndEndIsAfterWithPagination(@Param("itemIds") List<Long> itemIds,
                                                                            @Param("start") LocalDateTime start,
                                                                            @Param("end") LocalDateTime end,
                                                                            Pageable pageable);

    // Поиск бронирований по списку предметов и статусу
    List<Booking> findByItemIdInAndStatusIs(List<Long> itemIds, Status status, Sort sort);

    // Поиск бронирований по списку предметов и статусу с пагинацией - НОВЫЙ МЕТОД
    @Query("SELECT b FROM Booking b WHERE b.item.id IN :itemIds AND b.status = :status")
    List<Booking> findByItemIdInAndStatusIsWithPagination(@Param("itemIds") List<Long> itemIds,
                                                          @Param("status") Status status,
                                                          Pageable pageable);

    // Поиск последнего бронирования для предмета
    @Query("select b from Booking b where b.item.id = :itemId and b.start < :time and b.status = 'APPROVED' order by b.end desc")
    List<Booking> findLastBookingByItemId(@Param("itemId") Long itemId, @Param("time") LocalDateTime time);

    // Поиск последних бронирований для списка предметов - НОВЫЙ МЕТОД для избежания N+1
    @Query("select b from Booking b where b.item.id in :itemIds and b.start < :time and b.status = 'APPROVED'")
    List<Booking> findLastBookingsByItemIds(@Param("itemIds") List<Long> itemIds, @Param("time") LocalDateTime time);

    // Поиск следующего бронирования для предмета
    @Query("select b from Booking b where b.item.id = :itemId and b.start > :time and b.status = 'APPROVED' order by b.start")
    List<Booking> findNextBookingByItemId(@Param("itemId") Long itemId, @Param("time") LocalDateTime time);

    // Поиск следующих бронирований для списка предметов - НОВЫЙ МЕТОД для избежания N+1
    @Query("select b from Booking b where b.item.id in :itemIds and b.start > :time and b.status = 'APPROVED'")
    List<Booking> findNextBookingsByItemIds(@Param("itemIds") List<Long> itemIds, @Param("time") LocalDateTime time);

    // Поиск завершенных бронирований пользователя для предмета
    @Query("select b.id from Booking b where b.booker.id = :bookerId and b.item.id = :itemId and b.end < :end and b.status = :status")
    List<Long> findByBookerIdAndItemIdAndEndIsBeforeAndStatusIs(@Param("bookerId") Long bookerId,
                                                                @Param("itemId") Long itemId, @Param("end") LocalDateTime end,
                                                                @Param("status") Status status);

    // Поиск активных бронирований пользователя для предмета
    @Query("select b.id from Booking b where b.booker.id = :bookerId and b.item.id = :itemId and b.start < :start and b.status = :status")
    List<Long> findByBookerIdAndItemIdAndStartIsBeforeAndStatusIs(@Param("bookerId") Long bookerId,
                                                                  @Param("itemId") Long itemId, @Param("start") LocalDateTime start,
                                                                  @Param("status") Status status);

    // Поиск бронирований пользователя для предмета по статусу
    @Query("select b.id from Booking b where b.booker.id = :bookerId and b.item.id = :itemId and b.status = :status")
    List<Long> findByBookerIdAndItemIdAndStatusIs(@Param("bookerId") Long bookerId,
                                                  @Param("itemId") Long itemId, @Param("status") Status status);
}