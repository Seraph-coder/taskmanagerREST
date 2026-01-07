package ru.naujava.taskmanager.repository.ydb;

import org.springframework.stereotype.Repository;
import ru.naujava.taskmanager.model.ydb.YdbTask;
import tech.ydb.table.SessionRetryContext;
import tech.ydb.table.query.Params;
import tech.ydb.table.result.ResultSetReader;
import tech.ydb.table.values.PrimitiveValue;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с задачами в YDB.
 */
@Repository
public class YdbTaskRepository extends BaseYdbRepository {

    public YdbTaskRepository(SessionRetryContext retryCtx) {
        super(retryCtx);
    }

    /**
     * Сохраняет задачу (создает или обновляет).
     */
    public YdbTask save(YdbTask task) {
        if (task.getId() == null) {
            task.setId(generateId("tasks"));
            task.setCreatedAt(Instant.now());
        }
        task.setUpdatedAt(Instant.now());

        String query = """
                DECLARE $id AS Uint64;
                DECLARE $description AS Utf8;
                DECLARE $is_done AS Bool;
                DECLARE $created_at AS Timestamp;
                DECLARE $updated_at AS Timestamp;
                DECLARE $user_id AS Uint64;
                UPSERT INTO tasks (id, description, is_done, created_at, updated_at, user_id)
                VALUES ($id, $description, $is_done, $created_at, $updated_at, $user_id);
                """;

        Params params = Params.of(
                "$id", PrimitiveValue.newUint64(task.getId()),
                "$description", PrimitiveValue.newText(task.getDescription()),
                "$is_done", PrimitiveValue.newBool(task.getIsDone()),
                "$created_at", PrimitiveValue.newTimestamp(task.getCreatedAt()),
                "$updated_at", PrimitiveValue.newTimestamp(task.getUpdatedAt()),
                "$user_id", PrimitiveValue.newUint64(task.getUserId())
        );

        executeUpdate(query, params);
        return task;
    }

    /**
     * Находит задачу по ID (по первичному ключу — можно SerializableRW).
     */
    public Optional<YdbTask> findById(Long id) {
        String query = """
                DECLARE $id AS Uint64;
                SELECT id, description, is_done, created_at, updated_at, user_id
                FROM tasks WHERE id = $id;
                """;

        Params params = Params.of("$id", PrimitiveValue.newUint64(id));

        return executeSerializableReadSingle(query, params, this::mapToTask);
    }

    /**
     * Находит все задачи пользователя — использует асинхронный индекс → StaleRO!
     */
    public List<YdbTask> findByUserId(Long userId) {
        String query = """
                DECLARE $user_id AS Uint64;
                SELECT id, description, is_done, created_at, updated_at, user_id
                FROM tasks VIEW idx_tasks_user_id
                WHERE user_id = $user_id
                ORDER BY id ASC;
                """;

        Params params = Params.of("$user_id", PrimitiveValue.newUint64(userId));

        return executeStaleReadList(query, params, this::mapToTask);
    }

    /**
     * Находит все задачи (без фильтра по индексу — можно SerializableRW).
     */
    public List<YdbTask> findAll() {
        String query = """
                SELECT id, description, is_done, created_at, updated_at, user_id
                FROM tasks
                ORDER BY id ASC;
                """;

        return executeSerializableReadList(query, Params.empty(), this::mapToTask);
    }

    /**
     * Находит задачи пользователя по статусу — использует асинхронный индекс → StaleRO!
     */
    public List<YdbTask> findByUserIdAndIsDone(Long userId, boolean isDone) {
        String query = """
                DECLARE $user_id AS Uint64;
                DECLARE $is_done AS Bool;
                SELECT id, description, is_done, created_at, updated_at, user_id
                FROM tasks VIEW idx_tasks_user_id_done
                WHERE user_id = $user_id AND is_done = $is_done
                ORDER BY id ASC;
                """;

        Params params = Params.of(
                "$user_id", PrimitiveValue.newUint64(userId),
                "$is_done", PrimitiveValue.newBool(isDone)
        );

        return executeStaleReadList(query, params, this::mapToTask);
    }

    /**
     * Обновляет статус задачи.
     */
    public void updateStatus(Long id, boolean isDone) {
        String query = """
                DECLARE $id AS Uint64;
                DECLARE $is_done AS Bool;
                DECLARE $updated_at AS Timestamp;
                UPDATE tasks SET is_done = $is_done, updated_at = $updated_at WHERE id = $id;
                """;

        Params params = Params.of(
                "$id", PrimitiveValue.newUint64(id),
                "$is_done", PrimitiveValue.newBool(isDone),
                "$updated_at", PrimitiveValue.newTimestamp(Instant.now())
        );

        executeUpdate(query, params);
    }

    /**
     * Удаляет задачу по ID.
     */
    public void deleteById(Long id) {
        String query = """
                DECLARE $id AS Uint64;
                DELETE FROM tasks WHERE id = $id;
                """;

        Params params = Params.of("$id", PrimitiveValue.newUint64(id));

        executeUpdate(query, params);
    }

    /**
     * Удаляет все задачи пользователя (использует индекс → но это запись, можно SerializableRW).
     */
    public void deleteByUserId(Long userId) {
        String query = """
                DECLARE $user_id AS Uint64;
                DELETE FROM tasks WHERE user_id = $user_id;
                """;

        Params params = Params.of("$user_id", PrimitiveValue.newUint64(userId));

        executeUpdate(query, params);
    }

    /**
     * Подсчитывает количество задач пользователя — можно через StaleRO (быстрее и безопасно).
     */
    public long countByUserId(Long userId) {
        String query = """
                DECLARE $user_id AS Uint64;
                SELECT COUNT(*) AS cnt FROM tasks VIEW idx_tasks_user_id WHERE user_id = $user_id;
                """;

        Params params = Params.of("$user_id", PrimitiveValue.newUint64(userId));

        return executeStaleReadSingle(query, params, rs -> rs.getColumn("cnt").getUint64())
                .orElse(0L);
    }

    /**
     * Маппинг результата в YdbTask.
     */
    private YdbTask mapToTask(ResultSetReader rs) {
        return new YdbTask(
                rs.getColumn("id").getUint64(),
                rs.getColumn("description").getText(),
                rs.getColumn("is_done").getBool(),
                rs.getColumn("created_at").getTimestamp(),
                rs.getColumn("updated_at").getTimestamp(),
                rs.getColumn("user_id").getUint64()
        );
    }
}