package ru.practicum.shareit.item.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    // Поиск предметов по ID владельца
    List<Item> findByOwnerId(Long ownerId);

    // Поиск предметов по ID запроса
    List<Item> findByRequestId(Long requestId);

    // Поиск доступных предметов по названию или описанию (без учета регистра)
    @Query("""
            select i from Item i
            where (upper(i.name) like upper(concat('%', ?1, '%'))
                  or upper(i.description) like upper(concat('%', ?1, '%')))
                  and i.isAvailable = true
            """)
    List<Item> searchAvailableItems(String text);
}