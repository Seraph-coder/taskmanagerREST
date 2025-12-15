package ru.naujava.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.naujava.taskmanager.entity.Task;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для управления задачами.
 */
public interface TaskRepository extends JpaRepository<Task, Long> {
    /**
     * Находит все задачи пользователя и сортирует их по id по возрастанию.
     */
    List<Task> findByUser_IdOrderByIdAsc(Long userId);

    /**
     * Находит все невыполненные задачи пользователя.
     */
    List<Task> findByUser_IdAndIsDoneFalseOrderByIdAsc(Long userId);

    /**
     * Находит все выполненные задачи пользователя.
     */
    List<Task> findByUser_IdAndIsDoneTrueOrderByIdAsc(Long userId);

    /**
     * Находит задачу по её ID и ID пользователя.
     */
    Optional<Task> findByIdAndUser_Id(Long id, Long userId);

    /**
     * Проверяет, существует ли задача с таким описанием у указанного пользователя.
     */
    boolean existsByUser_IdAndDescription(Long userId, String description);
}
