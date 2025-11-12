package ru.practicum.shareit.item.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Поиск комментариев по ID предмета с сортировкой по дате создания (новые первыми)
    List<Comment> findByItemIdOrderByCreatedDesc(Long itemId);
}