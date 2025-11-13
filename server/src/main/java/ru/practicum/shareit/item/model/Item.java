package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Entity
@Table(name = "items")
@Getter
@Setter
public class Item {
    // Уникальный идентификатор предмета
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Владелец предмета - LAZY загрузка для избежания N+1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    // Запрос на создание предмета - LAZY загрузка для избежания N+1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private ItemRequest request;

    // Название предмета
    @Column
    private String name;

    // Описание предмета
    @Column
    private String description;

    // Доступность предмета для бронирования
    @Column(name = "is_available")
    private Boolean isAvailable;

    // Список комментариев к предмету - LAZY загрузка для избежания N+1
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments;
}