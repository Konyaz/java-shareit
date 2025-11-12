package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
public class Comment {
    // Уникальный идентификатор комментария
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Текст комментария
    @Column
    private String text;

    // Предмет к которому относится комментарий
    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    // Автор комментария
    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    // Дата и время создания комментария
    @Column
    private LocalDateTime created;
}