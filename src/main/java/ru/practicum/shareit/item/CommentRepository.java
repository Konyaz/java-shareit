package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Находит все комментарии для вещи по её ID с загрузкой информации об авторе
     *
     * @param itemId ID вещи
     * @return список комментариев
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.item.id = :itemId ORDER BY c.created DESC")
    List<Comment> findByItemIdWithAuthor(@Param("itemId") Long itemId);

    /**
     * Находит все комментарии для списка вещей с загрузкой информации об авторе
     *
     * @param itemIds список ID вещей
     * @return список комментариев с информацией об авторе
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.item.id IN :itemIds ORDER BY c.created DESC")
    List<Comment> findByItemIdInWithAuthor(@Param("itemIds") List<Long> itemIds);

    /**
     * Находит все комментарии для вещи по её ID
     *
     * @param itemId ID вещи
     * @return список комментариев
     */
    List<Comment> findByItemIdOrderByCreatedDesc(Long itemId);

    /**
     * Находит все комментарии для списка вещей
     *
     * @param itemIds список ID вещей
     * @return список комментариев
     */
    List<Comment> findByItemIdInOrderByCreatedDesc(List<Long> itemIds);
}