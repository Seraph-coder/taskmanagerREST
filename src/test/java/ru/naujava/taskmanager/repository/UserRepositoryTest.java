package ru.naujava.taskmanager.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.naujava.taskmanager.entity.Role;
import ru.naujava.taskmanager.entity.User;

/**
 * Тесты для репозитория пользователей {@link UserRepository}.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    /**
     * Проверяет сохранение и поиск пользователя по имени.
     * <br>
     * Ожидаемое поведение: пользователь сохраняется и находится по имени.
     */
    @Test
    public void saveAndFindByUsername() {
        User user = new User("testuser", "password", Role.USER);
        userRepository.save(user);

        var found = userRepository.findByUsername("testuser");
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals("testuser", found.get().getUsername());
    }

    /**
     * Проверяет поиск несуществующего пользователя.
     * <br>
     * Ожидаемое поведение: возвращает пустой Optional.
     */
    @Test
    public void findByUsernameNotFound() {
        var found = userRepository.findByUsername("nonexistent");
        Assertions.assertTrue(found.isEmpty());
    }
}
