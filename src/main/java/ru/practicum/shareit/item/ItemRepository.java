package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * Находит все вещи пользователя по его ID
     *
     * @param ownerId ID владельца
     * @return список вещей пользователя
     */
    List<Item> findByOwnerIdOrderById(Long ownerId);

    /**
     * Поиск доступных вещей по тексту в названии или описании
     *
     * @param text текст для поиска
     * @return список найденных вещей
     */
    @Query("SELECT i FROM Item i " +
            "WHERE i.isAvailable = true " +
            "AND (LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%')))")
    List<Item> searchAvailableItems(@Param("text") String text);

}