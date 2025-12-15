package ru.naujava.taskmanager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.naujava.taskmanager.entity.Task;
import ru.naujava.taskmanager.entity.User;
import ru.naujava.taskmanager.repository.TaskRepository;

import java.util.List;
import java.util.Objects;

/**
 * Сервис для управления задачами.
 */
@Service
@Transactional
public class TaskService {
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Находит все задачи пользователя.
     *
     * @param user пользователь
     * @return список задач
     */
    public List<Task> findAllTasksByUser(User user) {
        Objects.requireNonNull(user, "user не должен быть null");
        return taskRepository.findByUser_IdOrderByIdAsc(user.getId());
    }

    /**
     * Находит все невыполненные задачи пользователя.
     *
     * @param user пользователь
     * @return список невыполненных задач
     */
    public List<Task> findActiveTasksByUser(User user) {
        Objects.requireNonNull(user, "user не должен быть null");
        return taskRepository.findByUser_IdAndIsDoneFalseOrderByIdAsc(user.getId());
    }

    /**
     * Находит все выполненные задачи пользователя.
     *
     * @param user пользователь
     * @return список выполненных задач
     */
    public List<Task> findCompletedTasksByUser(User user) {
        Objects.requireNonNull(user, "user не должен быть null");
        return taskRepository.findByUser_IdAndIsDoneTrueOrderByIdAsc(user.getId());
    }

    /**
     * Создает задачу для пользователя.
     *
     * @param description описание задачи
     * @param user пользователь
     * @return созданная задача
     * @throws IllegalArgumentException если задача с таким описанием уже существует
     */
    public Task createTask(String description, User user) {
        Objects.requireNonNull(description, "description не должен быть null");
        Objects.requireNonNull(user, "user не должен быть null");
        if (description.trim().isEmpty()) {
            logger.warn("Попытка создать задачу с пустым описанием для пользователя {}", user.getUsername());
            throw new IllegalArgumentException("Описание задачи не может быть пустым");
        }

        boolean exists = taskRepository.existsByUser_IdAndDescription(user.getId(), description);
        if (exists) {
            logger.warn("Попытка создать задачу с дублирующимся описанием '{}' для пользователя {}",
                    description, user.getUsername());
            throw new IllegalArgumentException("Задача с описанием '" + description + "' уже существует");
        }

        Task task = new Task(description, user);
        Task savedTask = taskRepository.save(task);
        logger.info("Создана новая задача '{}' для пользователя {}", description, user.getUsername());
        return savedTask;
    }

    /**
     * Удаляет задачу по ID и пользователю.
     *
     * @param taskId ID задачи
     * @param user пользователь
     * @return удаленная задача
     * @throws IllegalArgumentException если задача не найдена
     */
    public Task deleteTaskByIdAndUser(Long taskId, User user) {
        Objects.requireNonNull(taskId, "taskId не должен быть null");
        Objects.requireNonNull(user, "user не должен быть null");

        Task task = taskRepository.findByIdAndUser_Id(taskId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Задача не найдена"));
        taskRepository.delete(task);
        logger.info("Удалена задача '{}' для пользователя {}", task.getDescription(), user.getUsername());
        return task;
    }

    /**
     * Отмечает задачу как выполненную.
     *
     * @param taskId ID задачи
     * @param user пользователь
     * @return обновленная задача
     * @throws IllegalArgumentException если задача не найдена
     */
    public Task markTaskAsDone(Long taskId, User user) {
        Objects.requireNonNull(taskId, "taskId не должен быть null");
        Objects.requireNonNull(user, "user не должен быть null");

        Task task = taskRepository.findByIdAndUser_Id(taskId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Задача не найдена"));
        task.setDone(true);
        Task savedTask = taskRepository.save(task);
        logger.info("Задача '{}' отмечена как выполненная для пользователя {}",
                task.getDescription(), user.getUsername());
        return savedTask;
    }

    /**
     * Отмечает задачу как не выполненную.
     *
     * @param taskId ID задачи
     * @param user пользователь
     * @return обновленная задача
     * @throws IllegalArgumentException если задача не найдена
     */
    public Task markTaskAsNotDone(Long taskId, User user) {
        Objects.requireNonNull(taskId, "taskId не должен быть null");
        Objects.requireNonNull(user, "user не должен быть null");

        Task task = taskRepository.findByIdAndUser_Id(taskId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Задача не найдена"));
        task.setDone(false);
        Task savedTask = taskRepository.save(task);
        logger.info("Задача '{}' возвращена в активные для пользователя {}",
                task.getDescription(), user.getUsername());
        return savedTask;
    }

    /**
     * Находит задачу по ID и пользователю.
     *
     * @param taskId ID задачи
     * @param user пользователь
     * @return найденная задача
     * @throws IllegalArgumentException если задача не найдена
     */
    public Task findTaskByIdAndUser(Long taskId, User user) {
        Objects.requireNonNull(taskId, "taskId не должен быть null");
        Objects.requireNonNull(user, "user не должен быть null");

        return taskRepository.findByIdAndUser_Id(taskId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Задача не найдена"));
    }

    /**
     * Обновить описание задачи.
     *
     * @param taskId ID задачи
     * @param newDescription новое описание
     * @param user пользователь
     * @return обновленная задача
     * @throws IllegalArgumentException если задача не найдена
     */
    public Task updateTaskDescription(Long taskId, String newDescription, User user) {
        Objects.requireNonNull(taskId, "taskId не должен быть null");
        Objects.requireNonNull(newDescription, "newDescription не должен быть null");
        Objects.requireNonNull(user, "user не должен быть null");
        if (newDescription.trim().isEmpty()) {
            logger.warn("Попытка обновить задачу с пустым описанием для пользователя {}", user.getUsername());
            throw new IllegalArgumentException("Описание задачи не может быть пустым");
        }

        Task task = taskRepository.findByIdAndUser_Id(taskId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Задача не найдена"));

        if (!newDescription.equals(task.getDescription()) &&
                taskRepository.existsByUser_IdAndDescription(user.getId(), newDescription)) {
            logger.warn("Попытка обновить задачу с дублирующимся описанием '{}' для пользователя {}",
                    newDescription, user.getUsername());
            throw new IllegalArgumentException("Задача с описанием '" + newDescription + "' уже существует");
        }

        String oldDescription = task.getDescription();
        task.setDescription(newDescription);
        Task savedTask = taskRepository.save(task);
        logger.info("Обновлено описание задачи '{}' на '{}' для пользователя {}",
                oldDescription, newDescription, user.getUsername());
        return savedTask;
    }
}
