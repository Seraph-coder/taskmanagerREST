package ru.naujava.taskmanager.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.naujava.taskmanager.entity.Role;
import ru.naujava.taskmanager.entity.Task;
import ru.naujava.taskmanager.entity.User;

import java.util.List;

/**
 * Тесты для сервиса задач {@link TaskService}.
 */
@SpringBootTest
@ActiveProfiles("test")
public class TaskServiceIntegrationTest {
    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    /**
     * Создает задачи и находит все задачи пользователя.
     * <br>
     * Ожидаемое поведение: Создает задачи и возвращает список задач, связанных с указанным пользователем.
     */
    @Test
    public void createTaskAndFindAllTasksByUser() {
        User user = userService.register("testuser1", "password", Role.USER);
        User anotherUser = userService.register("testuser2", "password", Role.USER);

        taskService.createTask("Задача целевого пользователя", user);
        taskService.createTask("Задача другого пользователя", anotherUser);
        taskService.createTask("Задача целевого пользователя 2", user);

        List<Task> tasks = taskService.findAllTasksByUser(user);

        Assertions.assertEquals(2, tasks.size());
        Assertions.assertTrue(tasks.stream()
                .map(Task::getDescription)
                .toList()
                .containsAll(List.of("Задача целевого пользователя", "Задача целевого пользователя 2")));
    }

    /**
     * Не находит задачу для несуществующего пользователя.
     * <br>
     * Ожидаемое поведение: возвращает пустой список.
     */
    @Test
    public void findAllTasksByUserNotFound() {
        User user = userService.register("testuser3", "password", Role.USER);

        List<Task> tasks = taskService.findAllTasksByUser(user);

        Assertions.assertTrue(tasks.isEmpty());
    }

    /**
     * Создает задачу с существующим описанием.
     * <br>
     * Ожидаемое поведение: выбрасывает IllegalArgumentException.
     */
    @Test
    public void createTaskWithExistingDescription() {
        User user = userService.register("testuser4", "password", Role.USER);

        taskService.createTask("Какая-то задача", user);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> taskService.createTask("Какая-то задача", user));

        Assertions.assertEquals(
                "Задача с описанием 'Какая-то задача' уже существует", exception.getMessage());
    }

    /**
     * Создает задачу с пустым описанием.
     * <br>
     * Ожидаемое поведение: выбрасывает IllegalArgumentException.
     */
    @Test
    public void createTaskWithEmptyDescription() {
        User user = userService.register("testuser5", "password", Role.USER);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> taskService.createTask("", user));

        Assertions.assertEquals("Описание задачи не может быть пустым", exception.getMessage());
    }

    /**
     * Удаляет задачу по ID и пользователю.
     * <br>
     * Ожидаемое поведение: задача удаляется.
     */
    @Test
    public void deleteTaskByIdAndUser() {
        User user = userService.register("testuser6", "password", Role.USER);
        Task task = taskService.createTask("Задача для удаления", user);

        Task deletedTask = taskService.deleteTaskByIdAndUser(task.getId(), user);

        Assertions.assertEquals("Задача для удаления", deletedTask.getDescription());
        List<Task> tasks = taskService.findAllTasksByUser(user);
        Assertions.assertTrue(tasks.isEmpty());
    }

    /**
     * Удаляет несуществующую задачу.
     * <br>
     * Ожидаемое поведение: выбрасывает IllegalArgumentException.
     */
    @Test
    public void deleteTaskByIdAndUserNotFound() {
        User user = userService.register("testuser7", "password", Role.USER);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> taskService.deleteTaskByIdAndUser(999L, user));

        Assertions.assertEquals("Задача не найдена", exception.getMessage());
    }

    /**
     * Отмечает задачу как выполненную.
     * <br>
     * Ожидаемое поведение: задача отмечена как выполненная.
     */
    @Test
    public void markTaskAsDone() {
        User user = userService.register("testuser8", "password", Role.USER);
        Task task = taskService.createTask("Задача для выполнения", user);

        Task doneTask = taskService.markTaskAsDone(task.getId(), user);

        Assertions.assertTrue(doneTask.isDone());
        List<Task> activeTasks = taskService.findActiveTasksByUser(user);
        Assertions.assertTrue(activeTasks.isEmpty());
        List<Task> completedTasks = taskService.findCompletedTasksByUser(user);
        Assertions.assertEquals(1, completedTasks.size());
    }

    /**
     * Отмечает несуществующую задачу как выполненную.
     * <br>
     * Ожидаемое поведение: выбрасывает IllegalArgumentException.
     */
    @Test
    public void markTaskAsDoneNotFound() {
        User user = userService.register("testuser9", "password", Role.USER);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> taskService.markTaskAsDone(999L, user));

        Assertions.assertEquals("Задача не найдена", exception.getMessage());
    }

    /**
     * Обновляет описание задачи.
     * <br>
     * Ожидаемое поведение: описание задачи обновляется.
     */
    @Test
    public void updateTaskDescription() {
        User user = userService.register("testuser10", "password", Role.USER);
        Task task = taskService.createTask("Original description", user);

        Task updatedTask =
                taskService.updateTaskDescription(task.getId(), "Updated description", user);

        Assertions.assertEquals("Updated description", updatedTask.getDescription());
        Task foundTask = taskService.findTaskByIdAndUser(task.getId(), user);
        Assertions.assertEquals("Updated description", foundTask.getDescription());
    }

    /**
     * Обновляет описание несуществующей задачи.
     * <br>
     * Ожидаемое поведение: выбрасывается IllegalArgumentException.
     */
    @Test
    public void updateTaskDescriptionNotFound() {
        User user = userService.register("testuser11", "password", Role.USER);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> taskService.updateTaskDescription(999L, "New description", user));

        Assertions.assertEquals("Задача не найдена", exception.getMessage());
    }

    /**
     * Отмечает задачу как не выполненную.
     * <br>
     * Ожидаемое поведение: задача отмечена как не выполненная.
     */
    @Test
    public void markTaskAsNotDone() {
        User user = userService.register("testuser12", "password", Role.USER);
        Task task = taskService.createTask("Task to uncomplete", user);
        taskService.markTaskAsDone(task.getId(), user);

        Task undoneTask = taskService.markTaskAsNotDone(task.getId(), user);

        Assertions.assertFalse(undoneTask.isDone());
        List<Task> activeTasks = taskService.findActiveTasksByUser(user);
        Assertions.assertEquals(1, activeTasks.size());
        List<Task> completedTasks = taskService.findCompletedTasksByUser(user);
        Assertions.assertEquals(0, completedTasks.size());
    }

    /**
     * Отмечает несуществующую задачу как не выполненную.
     * <br>
     * Ожидаемое поведение: выбрасывается IllegalArgumentException.
     */
    @Test
    public void markTaskAsNotDoneNotFound() {
        User user = userService.register("testuser13", "password", Role.USER);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> taskService.markTaskAsNotDone(999L, user));

        Assertions.assertEquals("Задача не найдена", exception.getMessage());
    }

    /**
     * Обновляет описание задачи на существующее.
     * <br>
     * Ожидаемое поведение: выбрасывается IllegalArgumentException.
     */
    @Test
    public void updateTaskDescriptionDuplicate() {
        User user = userService.register("testuser14", "password", Role.USER);
        taskService.createTask("Task 1", user);
        Task task2 = taskService.createTask("Task 2", user);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> taskService.updateTaskDescription(task2.getId(), "Task 1", user));

        Assertions.assertEquals("Задача с описанием 'Task 1' уже существует", exception.getMessage());
    }
}
