package ru.practicum.shareit.user.dao.impl;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Repository
public class UserDaoImplInMemory implements UserDao {
    private final HashMap<Long, User> db = new HashMap<>();
    private long currentId = 1L;

    private long getNextId() {
        return currentId++;
    }

    public User create(User userData) {
        userData.setId(getNextId());
        db.put(userData.getId(), userData);
        return userData;
    }

    public User update(User userData) {
        db.put(userData.getId(), userData);
        return userData;
    }

    public List<User> getList() {
        return new ArrayList<>(db.values());
    }

    public User getById(long userId) {
        return db.get(userId);
    }

    public Boolean exists(long userId) {
        return db.containsKey(userId);
    }

    public Boolean isEmailExists(String email) {
        return db.values().stream()
                .anyMatch((user) -> Objects.equals(user.getEmail(), email));
    }

    public void removeById(long itemId) {
        db.remove(itemId);
    }
}