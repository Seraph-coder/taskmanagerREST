package ru.naujava.taskmanager.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.naujava.taskmanager.entity.Role;
import ru.naujava.taskmanager.entity.Task;
import ru.naujava.taskmanager.entity.User;

import java.util.List;

/**
 * Тесты для репозитория задач {@link TaskRepository}.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
public class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Проверяет сохранение и поиск задач пользователя.
     * <br>
     * Ожидаемое поведение: задачи сохраняются и находятся по ID пользователя.
     */
    @Test
    public void saveAndFindByUserId() {
        User user = new User("testuser", "password", Role.USER);
        userRepository.save(user);

        Task task1 = new Task("Task 1", user);
        Task task2 = new Task("Task 2", user);
        taskRepository.save(task1);
        taskRepository.save(task2);

        List<Task> tasks = taskRepository.findByUser_IdOrderByIdAsc(user.getId());
        Assertions.assertEquals(2, tasks.size());
        Assertions.assertEquals("Task 1", tasks.get(0).getDescription());
        Assertions.assertEquals("Task 2", tasks.get(1).getDescription());
    }

    /**
     * Проверяет поиск активных задач.
     * <br>
     * Ожидаемое поведение: возвращает только активные задачи.
     */
    @Test
    public void findActiveTasks() {
        User user = new User("testuser", "password", Role.USER);
        userRepository.save(user);

        Task activeTask = new Task("Active Task", user);
        Task doneTask = new Task("Done Task", user);
        doneTask.setDone(true);
        taskRepository.save(activeTask);
        taskRepository.save(doneTask);

        List<Task> activeTasks = taskRepository.findByUser_IdAndIsDoneFalseOrderByIdAsc(user.getId());
        Assertions.assertEquals(1, activeTasks.size());
        Assertions.assertEquals("Active Task", activeTasks.getFirst().getDescription());
    }

    /**
     * Проверяет поиск выполненных задач.
     * <br>
     * Ожидаемое поведение: возвращает только выполненные задачи.
     */
    @Test
    public void findCompletedTasks() {
        User user = new User("testuser", "password", Role.USER);
        userRepository.save(user);

        Task activeTask = new Task("Active Task", user);
        Task doneTask = new Task("Done Task", user);
        doneTask.setDone(true);
        taskRepository.save(activeTask);
        taskRepository.save(doneTask);

        List<Task> completedTasks = taskRepository.findByUser_IdAndIsDoneTrueOrderByIdAsc(user.getId());
        Assertions.assertEquals(1, completedTasks.size());
        Assertions.assertEquals("Done Task", completedTasks.getFirst().getDescription());
    }

    /**
     * Проверяет поиск задачи по ID и пользователю.
     * <br>
     * Ожидаемое поведение: возвращает задачу, если она принадлежит пользователю.
     */
    @Test
    public void findByIdAndUserId() {
        User user = new User("testuser", "password", Role.USER);
        userRepository.save(user);

        Task task = new Task("Test Task", user);
        taskRepository.save(task);

        var found = taskRepository.findByIdAndUser_Id(task.getId(), user.getId());
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals("Test Task", found.get().getDescription());
    }

    /**
     * Проверяет существование задачи с описанием.
     * <br>
     * Ожидаемое поведение: возвращает true, если задача существует для пользователя.
     */
    @Test
    public void existsByUserIdAndDescription() {
        User user = new User("testuser", "password", Role.USER);
        userRepository.save(user);

        Task task = new Task("Test Task", user);
        taskRepository.save(task);

        boolean exists = taskRepository.existsByUser_IdAndDescription(user.getId(), "Test Task");
        Assertions.assertTrue(exists);

        boolean notExists = taskRepository.existsByUser_IdAndDescription(user.getId(), "Nonexistent");
        Assertions.assertFalse(notExists);
    }
}
