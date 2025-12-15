package ru.naujava.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.naujava.taskmanager.entity.User;

import java.util.Optional;

/**
 * Репозиторий для управления пользователями.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Находит пользователя по имени пользователя.
     */
    Optional<User> findByUsername(String username);

    /**
     * Проверяет существование пользователя по имени.
     */
    boolean existsByUsername(String username);
}
