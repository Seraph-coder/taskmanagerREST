package ru.naujava.taskmanager.repository;

import org.springframework.data.repository.ListCrudRepository;
import ru.naujava.taskmanager.entity.User;

import java.util.Optional;

/**
 * Репозиторий для управления пользователями.
 */
public interface UserRepository extends ListCrudRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
