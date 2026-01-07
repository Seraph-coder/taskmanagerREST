package ru.naujava.taskmanager.repository.adapter;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import ru.naujava.taskmanager.entity.Role;
import ru.naujava.taskmanager.entity.User;
import ru.naujava.taskmanager.model.ydb.YdbUser;
import ru.naujava.taskmanager.repository.UserRepository;
import ru.naujava.taskmanager.repository.ydb.YdbUserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Адаптер для UserRepository, который работает с YDB вместо JPA.
 */
@Repository
@Primary
public class YdbUserRepositoryAdapter implements UserRepository {

    private final YdbUserRepository ydbUserRepository;

    public YdbUserRepositoryAdapter(YdbUserRepository ydbUserRepository) {
        this.ydbUserRepository = ydbUserRepository;
    }

    // Кастомные методы
    @Override
    public Optional<User> findByUsername(String username) {
        return ydbUserRepository.findByUsername(username).map(this::toEntity);
    }

    @Override
    public boolean existsByUsername(String username) {
        return ydbUserRepository.existsByUsername(username);
    }

    // === Реализация ListCrudRepository ===

    @Override
    public <S extends User> S save(S entity) {
        YdbUser ydbUser = toYdbUser(entity);
        YdbUser saved = ydbUserRepository.save(ydbUser);
        entity.setId(saved.getId());
        return entity;
    }

    @Override
    public Optional<User> findById(Long id) {
        return ydbUserRepository.findById(id).map(this::toEntity);
    }

    @Override
    public boolean existsById(Long id) {
        return ydbUserRepository.findById(id).isPresent();
    }

    @Override
    public List<User> findAll() {
        return ydbUserRepository.findAll().stream()
                .map(this::toEntity)
                .toList();
    }

    @Override
    public List<User> findAllById(Iterable<Long> ids) {
        List<User> result = new ArrayList<>();
        for (Long id : ids) {
            ydbUserRepository.findById(id)
                    .ifPresent(yu -> result.add(toEntity(yu)));
        }
        return result;
    }

    @Override
    public long count() {
        // Если в YdbUserRepository есть свой count() — используй его
        // Иначе считаем через findAll()
        return findAll().size();
    }

    @Override
    public void deleteById(Long id) {
        ydbUserRepository.deleteById(id);
    }

    @Override
    public void delete(User entity) {
        ydbUserRepository.deleteById(entity.getId());
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        ids.forEach(ydbUserRepository::deleteById);
    }

    @Override
    public void deleteAll(Iterable<? extends User> entities) {
        entities.forEach(entity -> ydbUserRepository.deleteById(entity.getId()));
    }

    @Override
    public void deleteAll() {
        ydbUserRepository.findAll().forEach(yu -> ydbUserRepository.deleteById(yu.getId()));
    }

    @Override
    public <S extends User> List<S> saveAll(Iterable<S> entities) {
        List<S> result = new ArrayList<>();
        for (S entity : entities) {
            result.add(save(entity));
        }
        return result;
    }

    // === Конвертеры ===

    private User toEntity(YdbUser ydbUser) {
        User user = new User();
        user.setId(ydbUser.getId());
        user.setUsername(ydbUser.getUsername());
        user.setPassword(ydbUser.getPassword());
        user.setRole(Role.valueOf(ydbUser.getRole()));
        return user;
    }

    private YdbUser toYdbUser(User user) {
        YdbUser ydbUser = new YdbUser();
        ydbUser.setId(user.getId());
        ydbUser.setUsername(user.getUsername());
        ydbUser.setPassword(user.getPassword());
        ydbUser.setRole(user.getRole().name());
        return ydbUser;
    }
}