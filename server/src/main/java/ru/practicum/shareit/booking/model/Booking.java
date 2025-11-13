package ru.practicum.shareit.booking.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.dao.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
public class Booking {
    // Уникальный идентификатор бронирования
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Дата и время начала бронирования
    @Column(name = "start_date")
    private LocalDateTime start;

    // Дата и время окончания бронирования
    @Column(name = "end_date")
    private LocalDateTime end;

    // Предмет, который бронируется - LAZY загрузка для избежания N+1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    // Пользователь, который бронирует предмет - LAZY загрузка для избежания N+1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booker_id")
    private User booker;

    // Статус бронирования
    @Enumerated(EnumType.STRING)
    private Status status;
}