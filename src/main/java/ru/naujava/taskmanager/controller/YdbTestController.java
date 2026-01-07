package ru.naujava.taskmanager.controller;

import org.springframework.web.bind.annotation.*;
import ru.naujava.taskmanager.entity.Role;
import ru.naujava.taskmanager.entity.Task;
import ru.naujava.taskmanager.entity.User;
import ru.naujava.taskmanager.repository.TaskRepository;
import ru.naujava.taskmanager.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Тестовый контроллер для проверки работы с YDB.
 * Удалите его после тестирования.
 */
@RestController
@RequestMapping("/api/test/ydb")
public class YdbTestController {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public YdbTestController(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    /**
     * Тест подключения к YDB.
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        try {
            long userCount = userRepository.count();
            long taskCount = taskRepository.count();

            result.put("status", "OK");
            result.put("userCount", userCount);
            result.put("taskCount", taskCount);
            result.put("message", "YDB connection successful!");
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * Создает тестового пользователя.
     */
    @PostMapping("/create-test-user")
    public Map<String, Object> createTestUser(@RequestParam String username) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (userRepository.existsByUsername(username)) {
                result.put("status", "EXISTS");
                result.put("message", "User already exists");
                return result;
            }

            User user = new User(username, "password123", Role.USER);
            User saved = userRepository.save(user);

            result.put("status", "CREATED");
            result.put("user", Map.of(
                    "id", saved.getId(),
                    "username", saved.getUsername(),
                    "role", saved.getRole()
            ));
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * Создает тестовую задачу для пользователя.
     */
    @PostMapping("/create-test-task")
    public Map<String, Object> createTestTask(
            @RequestParam String username,
            @RequestParam String description) {

        Map<String, Object> result = new HashMap<>();
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));

            Task task = new Task(description, user);
            Task saved = taskRepository.save(task);

            result.put("status", "CREATED");
            result.put("task", Map.of(
                    "id", saved.getId(),
                    "description", saved.getDescription(),
                    "isDone", saved.isDone(),
                    "userId", saved.getUser().getId()
            ));
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * Получает все задачи пользователя.
     */
    @GetMapping("/user-tasks")
    public Map<String, Object> getUserTasks(@RequestParam String username) {
        Map<String, Object> result = new HashMap<>();
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));

            List<Task> tasks = taskRepository.findByUser_IdOrderByIdAsc(user.getId());

            result.put("status", "OK");
            result.put("username", username);
            result.put("taskCount", tasks.size());
            result.put("tasks", tasks.stream().map(task -> Map.of(
                    "id", task.getId(),
                    "description", task.getDescription(),
                    "isDone", task.isDone()
            )).toList());
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * Получает всех пользователей.
     */
    @GetMapping("/users")
    public Map<String, Object> getAllUsers() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<User> users = userRepository.findAll();

            result.put("status", "OK");
            result.put("count", users.size());
            result.put("users", users.stream().map(user -> Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "role", user.getRole()
            )).toList());
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
        }
        return result;
    }
}