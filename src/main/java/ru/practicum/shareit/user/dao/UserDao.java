package ru.practicum.shareit.user.dao;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserDao {
    User create(User itemData);

    User update(User itemData);

    List<User> getList();

    User getById(long itemId);

    Boolean exists(long itemId);

    Boolean isEmailExists(String email);

    void removeById(long itemId);
}