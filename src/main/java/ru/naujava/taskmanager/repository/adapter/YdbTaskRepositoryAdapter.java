package ru.naujava.taskmanager.repository.adapter;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import ru.naujava.taskmanager.entity.Task;
import ru.naujava.taskmanager.entity.User;
import ru.naujava.taskmanager.model.ydb.YdbTask;
import ru.naujava.taskmanager.repository.TaskRepository;
import ru.naujava.taskmanager.repository.ydb.YdbTaskRepository;
import ru.naujava.taskmanager.repository.ydb.YdbUserRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Адаптер для TaskRepository, который работает с YDB вместо JPA.
 */
@Repository
@Primary
public class YdbTaskRepositoryAdapter implements TaskRepository {

    private final YdbTaskRepository ydbTaskRepository;
    private final YdbUserRepository ydbUserRepository;

    public YdbTaskRepositoryAdapter(YdbTaskRepository ydbTaskRepository,
                                    YdbUserRepository ydbUserRepository) {
        this.ydbTaskRepository = ydbTaskRepository;
        this.ydbUserRepository = ydbUserRepository;
    }

    // === Кастомные методы из TaskRepository ===

    @Override
    public List<Task> findByUser_IdOrderByIdAsc(Long userId) {
        return ydbTaskRepository.findByUserId(userId).stream()
                .map(this::toEntity)
                .toList();
    }

    @Override
    public List<Task> findByUser_IdAndIsDoneFalseOrderByIdAsc(Long userId) {
        return ydbTaskRepository.findByUserIdAndIsDone(userId, false).stream()
                .map(this::toEntity)
                .toList();
    }

    @Override
    public List<Task> findByUser_IdAndIsDoneTrueOrderByIdAsc(Long userId) {
        return ydbTaskRepository.findByUserIdAndIsDone(userId, true).stream()
                .map(this::toEntity)
                .toList();
    }

    @Override
    public Optional<Task> findByIdAndUser_Id(Long id, Long userId) {
        return ydbTaskRepository.findById(id)
                .filter(task -> task.getUserId().equals(userId))
                .map(this::toEntity);
    }

    @Override
    public boolean existsByUser_IdAndDescription(Long userId, String description) {
        return ydbTaskRepository.findByUserId(userId).stream()
                .anyMatch(task -> task.getDescription().equals(description));
    }

    // === Реализация CrudRepository ===

    @Override
    public <S extends Task> S save(S task) {
        YdbTask ydbTask = toYdbTask(task);
        YdbTask saved = ydbTaskRepository.save(ydbTask);
        task.setId(saved.getId());
        return task;
    }

    @Override
    public Optional<Task> findById(Long id) {
        return ydbTaskRepository.findById(id).map(this::toEntity);
    }

    @Override
    public boolean existsById(Long id) {
        return ydbTaskRepository.findById(id).isPresent();
    }

    @Override
    public List<Task> findAll() {  // ← изменил Iterable → List
        return ydbTaskRepository.findAll().stream()
                .map(this::toEntity)
                .toList();
    }

    @Override
    public List<Task> findAllById(Iterable<Long> ids) {  // ← изменил Iterable → List
        List<Task> result = new ArrayList<>();
        for (Long id : ids) {
            ydbTaskRepository.findById(id)
                    .ifPresent(ydbTask -> result.add(toEntity(ydbTask)));
        }
        return result;
    }

    @Override
    public <S extends Task> List<S> saveAll(Iterable<S> tasks) {  // ← изменил Iterable → List
        List<S> result = new ArrayList<>();
        for (S task : tasks) {
            result.add(save(task));
        }
        return result;
    }

    @Override
    public long count() {
        return ydbTaskRepository.findAll().size();
    }

    @Override
    public void deleteById(Long id) {
        ydbTaskRepository.deleteById(id);
    }

    @Override
    public void delete(Task task) {
        ydbTaskRepository.deleteById(task.getId());
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        ids.forEach(ydbTaskRepository::deleteById);
    }

    @Override
    public void deleteAll(Iterable<? extends Task> tasks) {
        tasks.forEach(task -> ydbTaskRepository.deleteById(task.getId()));
    }

    @Override
    public void deleteAll() {
        ydbTaskRepository.findAll().forEach(t -> ydbTaskRepository.deleteById(t.getId()));
    }

    // === Конвертеры ===

    private Task toEntity(YdbTask ydbTask) {
        Task task = new Task();
        task.setId(ydbTask.getId());
        task.setDescription(ydbTask.getDescription());
        task.setDone(ydbTask.getIsDone());
        task.setCreatedAt(LocalDateTime.ofInstant(ydbTask.getCreatedAt(), ZoneOffset.UTC));
        task.setUpdatedAt(LocalDateTime.ofInstant(ydbTask.getUpdatedAt(), ZoneOffset.UTC));

        // Загрузка пользователя (только необходимые поля)
        User user = ydbUserRepository.findById(ydbTask.getUserId())
                .map(ydbUser -> {
                    User u = new User();
                    u.setId(ydbUser.getId());
                    u.setUsername(ydbUser.getUsername());
                    u.setPassword(ydbUser.getPassword());
                    // role не нужен для задач — можно не заполнять
                    return u;
                })
                .orElse(null);
        task.setUser(user);

        return task;
    }

    private YdbTask toYdbTask(Task task) {
        YdbTask ydbTask = new YdbTask();
        ydbTask.setId(task.getId());
        ydbTask.setDescription(task.getDescription());
        ydbTask.setIsDone(task.isDone());

        if (task.getCreatedAt() != null) {
            ydbTask.setCreatedAt(task.getCreatedAt().toInstant(ZoneOffset.UTC));
        }
        if (task.getUpdatedAt() != null) {
            ydbTask.setUpdatedAt(task.getUpdatedAt().toInstant(ZoneOffset.UTC));
        }

        if (task.getUser() != null && task.getUser().getId() != null) {
            ydbTask.setUserId(task.getUser().getId());
        }

        return ydbTask;
    }
}