package ru.naujava.taskmanager.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import ru.naujava.taskmanager.entity.Role;
import ru.naujava.taskmanager.entity.User;

/**
 * Тесты для сервиса пользователей {@link UserService}.
 */
@SpringBootTest
@ActiveProfiles("test")
public class UserServiceIntegrationTest {
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Проверяет регистрацию и поиск пользователя по имени пользователя.
     * <br>
     * Ожидаемое поведение: регистрирует и возвращает пользователя.
     */
    @Test
    public void registerAndFindByUsername() {
        userService.register("testuser", "password", Role.USER);
        User foundUser = userService.findByUsername("testuser").orElseThrow();
        Assertions.assertEquals("testuser", foundUser.getUsername());
        Assertions.assertTrue(passwordEncoder.matches("password", foundUser.getPassword()));
        Assertions.assertEquals(Role.USER, foundUser.getRole());
    }

    /**
     * Проверяет регистрацию пользователя с существующим именем.
     * <br>
     * Ожидаемое поведение: выбрасывается IllegalArgumentException.
     */
    @Test
    public void registerWithExistingUsername() {
        userService.register("existinguser", "password", Role.USER);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.register("existinguser", "password", Role.USER));

        Assertions.assertEquals("Пользователь с таким именем уже существует", exception.getMessage());
    }

    /**
     * Проверяет поиск несуществующего пользователя.
     * <br>
     * Ожидаемое поведение: возвращает пустой Optional.
     */
    @Test
    public void findByUsernameNotFound() {
        var result = userService.findByUsername("nonexistent");
        Assertions.assertTrue(result.isEmpty());
    }

    /**
     * Проверяет изменение пароля пользователя.
     * <br>
     * Ожидаемое поведение: пароль изменяется.
     */
    @Test
    public void changePassword() {
        userService.register("changepassuser", "oldpassword", Role.USER);

        userService.changePassword("changepassuser", "oldpassword", "newpassword");

        User updatedUser = userService.findByUsername("changepassuser").orElseThrow();
        Assertions.assertTrue(passwordEncoder.matches("newpassword", updatedUser.getPassword()));
    }

    /**
     * Проверяет изменение пароля с неверным старым паролем.
     * <br>
     * Ожидаемое поведение: выбрасывается IllegalArgumentException.
     */
    @Test
    public void changePasswordWrongOldPassword() {
        userService.register("wrongpassuser", "correctpassword", Role.USER);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.changePassword(
                        "wrongpassuser", "wrongpassword", "newpassword"));

        Assertions.assertEquals("Старый пароль неверный", exception.getMessage());
    }

    /**
     * Регистрация пользователя с паролем короче 8 символов.
     * <br>
     * Ожидаемое поведение: выбрасывается IllegalArgumentException.
     */
    @Test
    public void registerPasswordTooShort() {
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.register("shortpass", "1234567", Role.USER));

        Assertions.assertEquals("Пароль должен содержать не менее 8 символов", exception.getMessage());
    }

    /**
     * Регистрация пользователя с пустым именем.
     * <br>
     * Ожидаемое поведение: выбрасывается IllegalArgumentException.
     */
    @Test
    public void registerEmptyUsername() {
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.register("", "password123", Role.USER));

        Assertions.assertEquals("Имя пользователя не может быть пустым", exception.getMessage());
    }

    /**
     * Проверяет изменение пароля для несуществующего пользователя.
     * <br>
     * Ожидаемое поведение: выбрасывается IllegalArgumentException.
     */
    @Test
    public void changePasswordNonExistentUser() {
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.changePassword(
                        "nonexistent", "old", "new"));

        Assertions.assertEquals("Пользователь не найден", exception.getMessage());
    }

    /**
     * Проверяет сохранение пользователя.
     * <br>
     * Ожидаемое поведение: пользователь сохраняется.
     */
    @Test
    public void saveUser() {
        User user = new User("saveuser", "password", Role.USER);
        User savedUser = userService.save(user);
        Assertions.assertNotNull(savedUser.getId());
        Assertions.assertEquals("saveuser", savedUser.getUsername());
    }
}
