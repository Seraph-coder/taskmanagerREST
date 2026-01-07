package ru.naujava.taskmanager.repository;

import org.springframework.data.repository.ListCrudRepository;  // ← именно этот!
import ru.naujava.taskmanager.entity.Task;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для управления задачами.
 */
public interface TaskRepository extends ListCrudRepository<Task, Long> {

    List<Task> findByUser_IdOrderByIdAsc(Long userId);

    List<Task> findByUser_IdAndIsDoneFalseOrderByIdAsc(Long userId);

    List<Task> findByUser_IdAndIsDoneTrueOrderByIdAsc(Long userId);

    Optional<Task> findByIdAndUser_Id(Long id, Long userId);

    boolean existsByUser_IdAndDescription(Long userId, String description);
}