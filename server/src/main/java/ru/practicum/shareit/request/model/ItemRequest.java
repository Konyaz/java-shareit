package ru.practicum.shareit.request.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "requests")
@Getter
@Setter
public class ItemRequest {
    // Уникальный идентификатор запроса
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Описание запроса
    @Column
    private String description;

    // Автор запроса - LAZY загрузка для избежания N+1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    // Дата и время создания запроса
    @Column
    private LocalDateTime created;

    // Список предметов, созданных в ответ на запрос - LAZY загрузка для избежания N+1
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Item> items;
}