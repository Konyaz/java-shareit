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

    @Override
    public User create(User userData) {
        userData.setId(getNextId());
        db.put(userData.getId(), userData);
        return userData;
    }

    @Override
    public User update(User userData) {
        db.put(userData.getId(), userData);
        return userData;
    }

    @Override
    public List<User> getList() {
        return new ArrayList<>(db.values());
    }

    @Override
    public User getById(long userId) {
        return db.get(userId);
    }

    @Override
    public Boolean exists(long userId) {
        return db.containsKey(userId);
    }

    @Override
    public Boolean isEmailExists(String email) {
        return db.values().stream()
                .anyMatch((user) -> Objects.equals(user.getEmail(), email));
    }

    @Override
    public void removeById(long userId) {
        db.remove(userId);
    }
}