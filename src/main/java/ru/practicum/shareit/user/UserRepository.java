package ru.practicum.shareit.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Проверяет существование пользователя с указанным email
     *
     * @param email email для проверки
     * @return true если пользователь с таким email существует
     */
    Boolean existsByEmail(String email);

    /**
     * Находит пользователя по email
     *
     * @param email email пользователя
     * @return Optional с пользователем
     */
    Optional<User> findByEmail(String email);
}