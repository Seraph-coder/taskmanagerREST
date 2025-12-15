package ru.naujava.taskmanager.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.naujava.taskmanager.entity.Role;
import ru.naujava.taskmanager.entity.User;
import ru.naujava.taskmanager.repository.UserRepository;

import java.util.Objects;
import java.util.Optional;

/**
 * Сервис для управления пользователями.
 */
@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Регистрирует нового пользователя.
     *
     * @param username имя пользователя
     * @param password пароль
     * @param role роль пользователя
     * @return зарегистрированный пользователь
     * @throws IllegalArgumentException если пользователь с таким именем уже существует или пароль слишком короткий
     */
    public User register(String username, String password, Role role) {
        Objects.requireNonNull(username, "username не должен быть null");
        Objects.requireNonNull(password, "password не должен быть null");
        Objects.requireNonNull(role, "role не должен быть null");

        if (username.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Пароль должен содержать не менее 8 символов");
        }

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Пользователь с таким именем уже существует");
        }

        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(username, encodedPassword, role);
        return userRepository.save(user);
    }

    /**
     * Находит пользователя по имени пользователя.
     *
     * @param username имя пользователя
     * @return Optional с пользователем или пустой Optional
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Сохраняет или обновляет пользователя.
     */
    public User save(User user) {
        Objects.requireNonNull(user, "Пользователь не может быть null");
        return userRepository.save(user);
    }

    /**
     * Изменить пароль пользователя.
     *
     * @param username имя пользователя
     * @param oldPassword старый пароль
     * @param newPassword новый пароль
     * @throws IllegalArgumentException если старый пароль неверный
     */
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Старый пароль неверный");
        }
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("Пароль должен содержать не менее 8 символов");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        save(user);
    }
}
